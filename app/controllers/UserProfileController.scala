package controllers

import java.util
import java.util.{Date, UUID}
import javax.inject.{Inject, Named}

import constants.FlashMsgConstants
import enums.RoleEnums
import models.formdata.{TagCheckboxForm, TagListForm, UserProfileDataForm, UserProfileOptionsForm}
import models.modelconstants.UserLevelScala
import models.profile.TagWord
import models.viewmodels._
import models._
import org.joda.time.DateTime
import play.api._
import play.api.data.Forms._
import play.api.data._
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc._
import securesocial.core.SecureSocial
import securesocial.core.SecureSocial.{RequestWithUser, SecuredRequest}
import services._
import customUtils.Helpers
import customUtils.authorization.WithRole

import scala.collection.JavaConverters._
import scala.collection.mutable
import customUtils.ViewedByMemberUtil
import customUtils.ViewedByUnKnownUtil
import customUtils.security.SecureSocialRuntimeEnvironment
import org.springframework.data.neo4j.support.Neo4jTemplate

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

class UserProfileController @Inject()(override implicit val env: SecureSocialRuntimeEnvironment,
                                      val template: Neo4jTemplate,
                                      val contentService: ContentService,
                                      val ratingController: RatingController,
                                      val likeController: LikeController,
                                      val messagesController: MessagesController,
                                      val favoritesController: FavoritesController,
                                      val userProfileService: UserProfileService,
                                      val tagWordService: TagWordService,
                                      val countyService: CountyService,
                                      val recipeService: RecipeService,
                                      val blogPostsService: BlogPostsService,
                                      val eventService: EventService,
                                      val ratingService: RatingService,
                                      val fileService: ContentFileService,
                                      val userCredentialService: UserCredentialService,
                                      val environment: Environment,
                                      val messageService: MessageService,
                                      val messagesApi: MessagesApi) extends Controller with SecureSocial with I18nSupport {

  // Forms
  val UserProfileValuesForms = Form(
    mapping(
      "name" -> text.verifying(Messages("profile.create.form.profilename.validation.required"), f => f.trim != ""),
      "name2" -> text,
      "aboutmeheadline" -> text,
      "aboutme" -> text,
      "county" -> text.verifying(Messages("profile.create.form.county.validation.required"), f => f.trim != ""),
      "streetAddress" -> text.verifying(Messages("profile.create.form.streetAddress.validation.required"), f => f.trim != ""),
      "zipCode" -> text.verifying(Messages("profile.create.form.zipCode.validation.required"), f => f.trim != ""),
      "city" -> text.verifying(Messages("profile.create.form.city.validation.required"), f => f.trim != ""),
      "phoneNumber" -> text.verifying(Messages("profile.create.form.phoneNumber.validation.required"), f => f.trim != ""),
      "personnummer" -> text.verifying(Messages("profile.personalidentitynumber.unique"), { g => isCorrectIdentificationNumber(g) }),
      "acceptTerms" -> boolean.verifying(Messages("profile.approve.memberterms"), h => h),
      "mainimage" -> optional(text),
      "avatarimage" -> optional(text),
      "firstName" -> text.verifying(Messages("profile.create.form.firstName.validation.required"), f => f.trim != ""),
      "lastName" -> text.verifying(Messages("profile.create.form.lastName.validation.required"), f => f.trim != ""),
      "emailAddress" -> text.verifying(Messages("profile.create.form.emailAddress.validation.required"), f => f.trim != ""),
      "emailAddress2" -> text,
      "options" -> mapping(
        "payCash" -> boolean,
        "paySwish" -> boolean,
        "payBankCard" -> boolean,
        "payIZettle" -> boolean,
        "wantsToBeHost" -> boolean
      )(UserProfileOptionsForm.apply)(UserProfileOptionsForm.unapply)
    )(UserProfileDataForm.apply)(UserProfileDataForm.unapply)
      verifying(Messages("profile.create.form.emailAddress.uniq"), e => isUniqueEmailAddress(e.emailAddress, e.emailAddress2))
      verifying(Messages("profile.control.unique"), f => isUniqueProfileName(f.name, f.name2))
      verifying(Messages("profile.control.host.must-have-paymentoption"), f => hasSelectedPaymentOption(f.options.wantsToBeHost, f.options.payCash, f.options.paySwish, f.options.payBankCard, f.options.payIZettle))
  )

  val TagsForm = Form(
    mapping(
      "tagList" -> optional(list[TagCheckboxForm] {
        mapping(
          "value" -> text
        )(TagCheckboxForm.apply)(TagCheckboxForm.unapply)
      })
    )(TagListForm.apply)(TagListForm.unapply)
  )

  private def isCorrectIdentificationNumber(id: String): Boolean = {
    if (id.length < 1) return false
    if (id.matches("[1-2][0-9]{11}")) return true
    if (id.matches("[0-9]{6}[-][0-9]{4}")) return true
    false
  }

  // Method checks if user wants to be host, then atleast one payment option has to be selected
  private def hasSelectedPaymentOption(wantsToBeHost: Boolean, payCash: Boolean, paySwish: Boolean, payBankCard: Boolean, payIZettle: Boolean): Boolean = {
    if (!wantsToBeHost) {
      return true
    } else {
      if (payCash || payBankCard || paySwish || payIZettle) {
        return true
      }
    }
    false
  }

  private def isUniqueProfileName(profileName: String, storedProfileName: String): Boolean = {

    // Länknamn måste börja på en bokstav, stor eller liten
    // därefter kan det komma en stor eller liten bokstav elln siffra eller ett bindesträck
    if (!profileName.matches("[a-z,A-Z]+[a-z,A-Z,0-9,-]*")) return false

    userProfileService.findByprofileLinkName(profileName) match {
      case None => true
      case Some(up) => {
        if (profileName == storedProfileName) {
          true
        } else {
          false
        }
      }
    }
  }

  private def isThisMyProfile(profile: UserProfile)(implicit request: RequestWithUser[AnyContent, UserCredential]): Boolean = {
    request.user match {
      case None =>
        false
      case Some(user) =>
        if (profile.getOwner.objectId == user.objectId)
          true
        else
          false
    }
  }

  private def isUniqueEmailAddress(emailAddress: String, storedEmailAddress: String): Boolean = {

    val emailRegExpPattern = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$"
    if (!emailAddress.matches(emailRegExpPattern)) return false

    // Fetch UserCredential with email address and autentication usernamn/password
    userCredentialService.findUserPasswordUserByEmail(emailAddress) match {
      case None => true
      case Some(up) => {
        // emailaddress has not been changed
        if (emailAddress == storedEmailAddress) {
          true
        } else {
          false
        }
      }
    }
  }


  def verifyUserProfile = SecuredAction(authorize = WithRole(RoleEnums.USER)) { implicit request: SecuredRequest[AnyContent, UserCredential] =>
    val curUser = request.user

    // Check so that all important information is filled, otherwise redirect to profile editing
    if (curUser.profiles.asScala.head.profileLinkName.isEmpty) {
      Redirect(routes.UserProfileController.edit()).flashing(FlashMsgConstants.Error -> Messages("profile.profilelinkname.isempty"))
    } else {
      Redirect(routes.StartPageController.index())
      //Redirect(routes.UserProfileController.viewProfileByName(curUser.profiles.asScala.head.profileLinkName))
    }
  }

  def viewProfileByName(profileName: String) = UserAwareAction() { implicit request =>


    // Try getting the profile from name, if failure show 404
    userProfileService.findByprofileLinkName(profileName) match {
      case Some(profile) =>

        val perf = customUtils.Helpers.startPerfLog()
        val profileOwner = profile.getOwner
        val myProfile = isThisMyProfile(profile)

        val dataAsync = for {
          messages <- Future(if (myProfile) buildMessageList(profileOwner) else None)
          recipeBoxes <- Future(recipeService.getRecipeBoxes(profileOwner))
          eventBoxCount <- Future(eventService.getCountOfMyEvents(profileOwner))
          bookingsMadeByMe <- Future(if (myProfile) eventService.getBookingsMadeByMe(profileOwner, this.getBaseUrl) else None)
          bookingsMadeToMyEvents <- Future(if (myProfile) eventService.getBookingsMadeToMyEvents(profileOwner, this.getBaseUrl) else None)
          myReviewBoxes <- Future(if (myProfile) ratingService.getMyUserReviews(profileOwner) else None)
          myRecipeReviewBoxes <- Future(if (myProfile) ratingService.getMyUserReviewsAboutFood(profileOwner) else None)
          reviewBoxesAboutMyFood <- Future(ratingService.getUserReviewsAboutMyFood(profileOwner))
          reviewBoxesAboutMe <- Future(ratingService.getUserReviewsAboutMe(profileOwner))
          tags <- Future(tagWordService.findByProfileAndGroup(profile, "profile"))
          metaData <- Future(buildMetaData(profile, request))
          shareUrl <- Future(createShareUrl(profile))
          userRateForm <- Future(ratingController.renderUserRateForm(profileOwner, routes.UserProfileController.viewProfileByName(profile.profileLinkName).url, request.user))
          userLikeForm <- Future(likeController.renderUserLikeForm(profileOwner, request.user))
          requestForm <- Future(messagesController.renderHostForm(profileOwner, request.user))
          favorites <- Future(if (myProfile) favoritesController.renderFavorites(profile) else None)
          visMemberCount <- Future(if (myProfile) Some(userProfileService.countViewsByMember(profile)) else None)
          visUnknownCount <- Future(if (myProfile) Some(userProfileService.countViewsByUnknown(profile)) else None)
          blogPostsCount <- Future(blogPostsService.countBlogPostsForUser(profile))
          userCredentialAverageRating <- Future(ratingService.getAverageRatingForUser(profile.getOwner.objectId))
          userCredentialNrOfTotalRatings <- Future(ratingService.getCountOfAllMemberRatingsForUser(profile.getOwner.objectId))

        } yield (recipeBoxes,
          eventBoxCount,
          bookingsMadeByMe,
          bookingsMadeToMyEvents,
          myReviewBoxes,
          myRecipeReviewBoxes,
          reviewBoxesAboutMyFood,
          reviewBoxesAboutMe,
          tags,
          messages,
          metaData,
          shareUrl,
          userRateForm,
          userLikeForm,
          requestForm,
          favorites,
          visMemberCount,
          visUnknownCount,
          blogPostsCount,
          userCredentialAverageRating,
          userCredentialNrOfTotalRatings
          )

        // Should the event be registered or not
        doDebugLogViewOfUserProfile(request, profile)
        doLogViewOfUserProfile(request, profile)

        val res = Await.result(dataAsync, Duration.Inf)
        customUtils.Helpers.endPerfLog("Profile:(" + profile.profileLinkName + ") - Loading time: ", perf)

        val model = ProfilePageModel(
          userProfile = profile,
          recipeBoxes = res._1,
          eventBoxCount = res._2,
          bookingsMadeByMe = res._3,
          bookingsMadeToMyEvents = res._4,
          myReviewBoxes = res._5,
          myRecipeReviewBoxes = res._6,
          reviewBoxesAboutMyFood = res._7,
          reviewBoxesAboutMe = res._8,
          tagList = res._9,
          userMessages = res._10,
          metaData = res._11,
          shareUrl = res._12,
          isThisMyProfile = myProfile,
          currentUser = request.user,
          userRateForm = res._13,
          userLikeForm = res._14,
          requestForm = res._15,
          favorites = res._16,
          visMemberCount = res._17,
          visUnknownCount = res._18,
          blogPostsCount = res._19,
          userCredentialAverageRating = res._20,
          userCredentialNrOfTotalRatings = res._21
        )

        Ok(views.html.profile.index(model))
      case None =>
        val errMess = "Cannot find user profile using name:" + profileName
        Logger.debug(errMess)
        NotFound(views.html.error.notfound(refUrl = request.path))
    }
  }

  private def doDebugLogViewOfUserProfile(request: RequestWithUser[AnyContent, UserCredential], profile: UserProfile) {

    if (!environment.mode.equals(Mode.Prod)) {
      val viewerProfile: Option[UserProfile] = request.user match {
        case Some(user) => Some(user.getUserProfile)
        case None => None
      }

      if (viewerProfile.isEmpty) {
        Logger.debug("Log access to userprofile: " + profile.getOwner.getFullName + " - Viewer: " + "Anonymous viewer")
      } else {
        Logger.debug("Log access to userprofile: " + profile.getOwner.getFullName + " - Viewer : " + viewerProfile.get.profileLinkName)
      }
    }

  }

  /**
    * Log the access to a user profile member
    * A loged on member or an unkown user accessing a profile
    *
    * @param request the users requesting viewing the profile
    * @param profile the user of the profile page
    */
  def doLogViewOfUserProfile(request: RequestWithUser[AnyContent, UserCredential], profile: UserProfile) {

    // Kontrollera att det är en inloggad användare
    if (request.user.isEmpty) {

      // Det är inte en inloggad användare
      val ipAddress = request.remoteAddress
      val util: ViewedByUnKnownUtil = new ViewedByUnKnownUtil()

      // Eftersom vi inte har något användarnamn får vi hämta ip-adress
      // Kontrollera om profilen dvs. den visade sisan har något objekt för att spara
      // undan icke inloggade användare
      val unkVisit = userProfileService.getViewedByUnKnown(profile)
      if (unkVisit.isDefined && unkVisit.get.getSize > 0) {


        // Number of days to store data as invidual logposts
        val oldestDate: Date = util.xDayEarlier(1)

        // Remove older recorded data and count number of accesss to the profile page
        //util.removeAllAccessOlderThen(oldestDate, log)

        //remove the sam ip-address if it is stored before
        util.removeOldAccessOfSameHost(ipAddress, unkVisit.get)

        // save access of profile to the profile users node for ...
        userProfileService.logUnKnownProfileViewByObjectId(unkVisit.get, ipAddress)
      } else {

        // Det finns inget objekt med icke inloggade användare skapa objektet
        var viewedByUnKnown = new ViewedByUnKnown()

        // Skapa ett besök av en okänd användare
        viewedByUnKnown.viewedBy(ipAddress, util.getNowString)
        profile.setViewedByUnKnown(viewedByUnKnown)
        userProfileService.saveUserProfile(profile)
      }

    } else {

      // inloggad användare försöker se på en profilsida.....

      // fetch logged in user
      val theUser: Option[UserProfile] = request.user match {
        case Some(user) => Some(user.getUserProfile)
        case None => None
      }

      val vOId: String = theUser match {
        case Some(v) => v.objectId.toString
        case None => ""
      }

      // Member access
      val memberVis = userProfileService.getViewedByMember(profile)
      if (memberVis.isDefined && memberVis.get.getSize > 0) {

        val util: ViewedByMemberUtil = new ViewedByMemberUtil()
        profile.setViewedByMeber(memberVis.get)

        // Number of days to store data as invidual logposts
        val oldestDate: Date = util.xDayEarlier(7)

        // Remove older recorded data and count number of accesss to the profile page
        //util.removeAllAccessOlderThen(oldestDate, log)

        // remove the same member if it have been viewn the same profile an other date
        util.removeOldAccessOfSameUser(profile.objectId.toString, memberVis.get)

        // save access of profile to the profile users node for ...
        userProfileService.logProfileViewByObjectId(memberVis.get, vOId, profile.objectId.toString)
      } else {
        // Om objektet inte finns lägger den till detta...
        val log = new ViewedByMember()
        userProfileService.logProfileViewByObjectId(log, vOId, profile.objectId.toString)
        profile.setViewedByMeber(log)
        userProfileService.saveUserProfile(profile)
      }

    }
  }


  def viewProfileByLoggedInUser = SecuredAction(authorize = WithRole(RoleEnums.USER)) { implicit request: SecuredRequest[AnyContent, UserCredential] =>

    userProfileService.findByOwner(request.user) match {
      case Some(profile) =>
        if (profile.profileLinkName.isEmpty) {
          Logger.debug("Profilelinkname is empty!")
          Redirect(routes.UserProfileController.edit()).flashing(FlashMsgConstants.Error -> Messages("profile.profilelinkname.isempty"))
        } else {

          Redirect(routes.UserProfileController.viewProfileByName(profile.profileLinkName)) // TODO: This causes double lookup, improve later
        }
      case None =>
        val errMess = "Cannot find user profile using current user:" + request.user.objectId
        Logger.debug(errMess)
        NotFound(views.html.error.notfound(refUrl = request.path))
    }
  }

  private def createShareUrl(profile: UserProfile): String = {
    routes.UserProfileController.viewProfileByName(profile.profileLinkName).url + "?ts=" + Helpers.getDateForSharing(profile)
  }


  private def buildMessageList(uc: UserCredential): Option[List[ReplyToGuestMessage]] = {
    messagesController.createListOfMessages(messageService.findIncomingMessagesForUser(uc), uc)
  }

  private def buildMetaData(profile: UserProfile, request: RequestHeader): Option[MetaData] = {

    val domain = "//" + request.domain

    Some(MetaData(
      fbUrl = domain + request.path,
      fbTitle = Messages("profile.title", profile.profileLinkName),
      fbDesc = profile.aboutMe match {
        case null | "" =>
          profile.aboutMeHeadline match {
            case null | "" => ""
            case item => customUtils.Helpers.limitLength(item, 125)
          }
        case item => {
          customUtils.Helpers.limitLength(Helpers.removeHtmlTags(item), 125)
        }
      },
      fbImage = profile.getMainImage match {
        case image: models.files.ContentFile => {
          domain + routes.ImageController.profileNormal(image.getStoreId).url
        }
        case _ => {
          domain + "/images/profile/profile-default-main-image.jpg"
        }
      }
    ))
  }

  /** **************************************************************************************************
    * Show userProfile for edit
    * display profile data for the current user to be changed
    * my profile
    * ***************************************************************************************************/
  def edit = SecuredAction(authorize = WithRole(RoleEnums.USER)) { implicit request: SecuredRequest[AnyContent, UserCredential] =>

    val userProfile = request.user.getUserProfile
    val identificationNumber = userProfile.getOwner.personNummer

    // Get HOST or not
    var isUserHost: Boolean = false
    if (userProfile.getRole.contains(UserLevelScala.HOST.Constant)) {
      isUserHost = true
    }

    // Tags
    val sortedTagList = getSortedTagWordList
    val userProfOldSavedTags = Option(userProfile.getTags.asScala.toList.map { tr => tr.tagWord })

    // County
    val countyObjectId: String = userProfile.getLocations.asScala match {
      case Nil | null => ""
      case countyRelList => countyRelList.headOption match {
        case None => ""
        case Some(countyRelation) => countyRelation.county.objectId.toString
      }
    }

    // Other values not fit to be in form-classes
    val mainImage = userProfile.getMainImage match {
      case null => None
      case image => Some(image.objectId.toString)
    }
    val avatarImage = userProfile.getAvatarImage match {
      case null => None
      case image => Some(image.objectId.toString)
    }

    val newTagForm = TagsForm.fill(TagListForm(
      remapListWithTagWordsToForm(userProfOldSavedTags)
    ))

    // Fill forms with stored values
    val newForm = UserProfileValuesForms.fillAndValidate(UserProfileDataForm(
      userProfile.profileLinkName,
      userProfile.profileLinkName,
      userProfile.aboutMeHeadline,
      userProfile.aboutMe,
      countyObjectId,
      userProfile.streetAddress,
      userProfile.zipCode,
      userProfile.city,
      userProfile.phoneNumber,
      identificationNumber,
      userProfile.isTermsOfUseApprovedAccepted,
      mainImage,
      avatarImage,
      userProfile.getOwner.firstName,
      userProfile.getOwner.lastName,
      userProfile.getOwner.emailAddress,
      userProfile.getOwner.emailAddress,
      UserProfileOptionsForm(
        userProfile.payCash,
        userProfile.paySwish,
        userProfile.payBankCard,
        userProfile.payIZettle,
        isUserHost
      )
    ))



    // Other values not fit to be in form-classes
    val extraValues = setExtraValues(userProfile)

    Ok(views.html.profile.editProfile(
      newForm,
      newTagForm,
      sortedTagList,
      optionsLocationAreas = countyService.getCounties,
      extraValues = extraValues,
      editingProfile = Some(userProfile),
      termsAndConditions = contentService.getTermsAndConditions))
  }

  private def getSortedTagWordList: Option[List[TagWord]] = {
    tagWordService.listByGroupOption("profile") match {
      case None => None
      case Some(items) => Some(items.sortBy(tw => tw.tagName))
    }
  }

  private def remapListWithTagWordsToForm(tagList: Option[List[TagWord]]): Option[List[TagCheckboxForm]] = {
    tagList match {
      case None => None
      case Some(tags) => Some(tags.map {
        t: TagWord =>
          TagCheckboxForm(t.objectId.toString)
      })
    }
  }


  private def setExtraValues(userProfile: UserProfile): EditProfileExtraValues = {
    // Other values not fit to be in form-classes
    val mainImage = userProfile.getMainImage match {
      case null => None
      case image => Some(image.getStoreId)
    }
    val avatarImage = userProfile.getAvatarImage match {
      case null => None
      case image => Some(image.getStoreId)
    }

    EditProfileExtraValues(
      mainImage match {
        case Some(item) => Some(List(routes.ImageController.imgChooserThumb(item).url))
        case None => None
      },
      avatarImage match {
        case Some(item) => Some(List(routes.ImageController.imgChooserThumb(item).url))
        case None => None
      },
      userProfile.getMaxNrOfMainImages,
      userProfile.getMaxNrOfAvatarImages
    )
  }

  private def safeJava(value: String): String = {
    var outString: String = ""

    if (value != null && value != None) {
      outString = value
    }

    outString
  }

  // add favorite
  def addFavorite(userCredentialObjectId: String) = SecuredAction() { implicit request: SecuredRequest[AnyContent, UserCredential] =>
    val friendsUserCredential = userCredentialService.findById(UUID.fromString(userCredentialObjectId))
    val myUserP = userProfileService.findByOwner(request.user)
    userProfileService.addFavorites(myUserP.get, friendsUserCredential.get)

    Ok("Ok")
  }

  // remove favorite
  def removeFavorite(userCredentialObjectId: String) = SecuredAction() { implicit request: SecuredRequest[AnyContent, UserCredential] =>
    val friendsUserCredential = userCredentialService.findById(UUID.fromString(userCredentialObjectId))
    val myUserP = userProfileService.findByOwner(request.user)
    userProfileService.removeFavorites(myUserP.get, friendsUserCredential.get.getUserProfile)

    Ok("Ok")
  }


  def isFavouriteToMe(userCredentialObjectId: String) = UserAwareAction() { implicit request =>

    val currentUser = request.user
    var retValue = ""

    val hasAccess = currentUser match {
      case Some(user) => true
      case None => false
    }

    if(hasAccess){

      val friendsUserCredential = userCredentialService.findById(UUID.fromString(userCredentialObjectId))

      if (friendsUserCredential.isDefined) {
        retValue = userProfileService.isFavouriteToMe(currentUser.get.getUserProfile, friendsUserCredential.get) match {
          case true => "YES"
          case false => "NO"
        }
      } else {
        retValue = "NO"
      }
    } else {
      retValue = "USER_NOT_LOGGED_IN"
    }
    Ok(retValue)
  }


  def showFavoritesPage = SecuredAction { implicit request =>
    Ok(views.html.profile.addAsFavorite(request.user.getUserProfile))
  }


  /** **************************************************************************************************
    * Save UserProfile
    * **************************************************************************************************/
  def editSubmit = SecuredAction(authorize = WithRole(RoleEnums.USER))(parse.multipartFormData) { implicit request =>

    var userCredential = request.user
    var userProfile = userCredential.getUserProfile

    var allTagsSelectedInForm: Option[List[TagCheckboxForm]] = None
    var countyId: String = ""
    var acceptTerms: Boolean = false
    var payBankCard: Boolean = false
    var payCash: Boolean = false
    var payIZettle: Boolean = false
    var paySwish: Boolean = false
    var wantsToBeHost: Boolean = false
    var emailAddress: String = ""

    // Fetch opt-form values
    val tagListForm = TagsForm.bindFromRequest.fold(
      error => {
        Logger.error("Error cannot read TagsForms")
        error
      },
      formValues => {
        allTagsSelectedInForm = formValues.tagList
        TagsForm.fill(TagListForm(allTagsSelectedInForm))
      }
    )

    // Fetch main-form values
    UserProfileValuesForms.bindFromRequest.fold(
      errors => {

        // Fetch all tags
        val sortedTagList = getSortedTagWordList

        // Other values not fit to be in form-classes
        val extraValues = setExtraValues(userProfile)

        BadRequest(views.html.profile.editProfile(
          profileDataForm = errors,
          tagListForm = tagListForm,
          listOfTags = sortedTagList,
          extraValues = extraValues,
          optionsLocationAreas = countyService.getCounties,
          termsAndConditions = contentService.getTermsAndConditions))
      },
      formValues => {
        emailAddress = formValues.emailAddress

        payCash = formValues.options.payCash
        payBankCard = formValues.options.payBankCard
        payIZettle = formValues.options.payIZettle
        paySwish = formValues.options.paySwish
        wantsToBeHost = formValues.options.wantsToBeHost

        // The users are always guests
        if (!userProfile.getRole.contains(UserLevelScala.GUEST.Constant)) {
          userProfile.getRole.add(UserLevelScala.GUEST.Constant)
        }

        // Add user to Host role
        wantsToBeHost match {
          case true =>
            userProfile = userProfileService.addUserAsHostIfNotAlready(userProfile)
          case false => {
            userProfile = userProfileService.removeUserAsHost(userProfile)
          }
        }


        // Save: UserCredential
        userCredential.personNummer = formValues.personnummer
        userCredential.firstName = formValues.firstName
        userCredential.lastName = formValues.lastName
        userCredential.fullName = formValues.firstName + " " + formValues.lastName
        userCredential.userId = emailAddress
        userCredential.emailAddress = emailAddress

        userCredentialService.save(userCredential)

        userProfile.aboutMeHeadline = formValues.aboutmeheadline
        userProfile.aboutMe = formValues.aboutme
        userProfile.profileLinkName = formValues.name
        userProfile.city = formValues.city
        userProfile.zipCode = formValues.zipCode
        userProfile.streetAddress = formValues.streetAddress
        userProfile.phoneNumber = formValues.phoneNumber
        userProfile.isTermsOfUseApprovedAccepted = formValues.acceptTerms
        userProfile.payBankCard = payBankCard
        userProfile.payCash = payCash
        userProfile.payIZettle = payIZettle
        userProfile.paySwish = paySwish

        /*
        theUserProfile.firstName = firstName
        theUserProfile.lastName = lastName
        userProfile.childFfriendly = childFfriendly
        userProfile.handicapFriendly = handicapFriendly
        userProfile.havePets = havePets
        userProfile.smoke = smoke
        userProfile.allkoholServing = allkoholServing
        userProfile.maxNoOfGuest = maxGuest
        userProfile.minNoOfGuest = uOptValues.minGuest
        */

        // Set County
        countyId = formValues.county
        if (countyId == null || countyId.trim().length < 2) {
          userProfile = userProfileService.removeAllLocationTags(userProfile)
        } else {
          countyService.findById(UUID.fromString(countyId)) match {
            case None =>
              userProfile = userProfileService.removeAllLocationTags(userProfile)
            case Some(item) =>
              userProfile = userProfileService.removeAllLocationTags(userProfile)
              userProfile = userProfileService.addLocation(userProfile, item)

          }
        }

        // Images
        // Avatar image
        formValues.avatarimage match {
          case Some(imageId) => UUID.fromString(imageId) match {
            case imageUUID: UUID =>
              fileService.getFileByObjectIdAndOwnerId(imageUUID, userCredential.objectId) match {
                case Some(item) => userProfile = userProfileService.setAndRemoveAvatarImage(userProfile, item)
                case _ => None
              }
          }
          case None => None
        }

        // Main image
        formValues.mainimage match {
          case Some(imageId) => UUID.fromString(imageId) match {
            case imageUUID: UUID =>
              fileService.getFileByObjectIdAndOwnerId(imageUUID, userCredential.objectId) match {
                case Some(item) => userProfile = userProfileService.setAndRemoveMainImage(userProfile, item)
                case _ => None
              }
          }
          case None => None
        }

        userProfile.keyIdentity = emailAddress + "_userpass"
        userProfile.userIdentity = emailAddress

        Logger.debug("UP:userIdentity: " + userProfile.userIdentity)
        Logger.debug("UP:keyIdentity: " + userProfile.keyIdentity)

        // Save tags
        // Verify tags that tags are UUID
        val tagsToSaveUUID: List[UUID] = allTagsSelectedInForm match {
          case None => Nil
          case Some(items) => items.map { tag =>
            UUID.fromString(tag.value)
          }
        }

        // Look tags up in the DB
        val tagListToSave: List[TagWord] = tagWordService.findByListOfId(tagsToSaveUUID) match {
          case None => Nil
          case Some(tagsToSave) => tagsToSave
        }

        // Save the changes to tags
        userProfileService.updateUserProfileTags(userProfile, tagListToSave)

        Redirect(routes.UserProfileController.edit()).flashing(FlashMsgConstants.Success -> Messages("profile.create.saved-successfully"))
      })
  }

  private def getBaseUrl()(implicit request: RequestHeader): String = {
    routes.StartPageController.index().absoluteURL(secure = request.secure).dropRight(1)
  }

}

package controllers

import java.util
import java.util.{Date, UUID}
import javax.inject.{Named, Inject}
import constants.FlashMsgConstants
import enums.RoleEnums
import models.formdata.{TagCheckboxForm, UserProfileOptionsForm, UserProfileDataForm}
import models.modelconstants.UserLevelScala
import models.profile.TagWord
import models.viewmodels._
import models._
import org.joda.time.DateTime
import play.api._
import play.api.data.Forms._
import play.api.data._
import play.api.i18n.{I18nSupport, MessagesApi, Messages}
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

class UserProfileController @Inject() (override implicit val env: SecureSocialRuntimeEnvironment,
                                       val contentService: ContentService,
                                       val ratingController: RatingController,
                                       val likeController: LikeController,
                                       val messagesController: MessagesController,
                                       val favoritesController: FavoritesController,
                                       val userProfileService: UserProfileService,
                                       val tagWordService: TagWordService,
                                       val countyService: CountyService,
                                       val recipeService: RecipeService,
                                       val eventService: EventService,
                                       val ratingService: RatingService,
                                       val fileService: ContentFileService,
                                       val userCredentialService: UserCredentialService,
                                       val environment: Environment,
                                       val messageService: MessageService,
                                       implicit val nodeEntityService: NodeEntityService,
                                       val messagesApi: MessagesApi) extends Controller with SecureSocial with I18nSupport {



  /*
  // Form
  val userProfileForm: play.api.data.Form[models.formdata.UserProfileForm] = play.api.data.Form(
    mapping(
      "userName" -> text,
      "emailAddress" -> email,
      "firstName" -> text,
      "lastName" -> text,
      "aboutme" -> text,
      // "quality" -> list(boolean),
      "county" -> text,
      "streetAddress" -> text,
      "zipCode" -> text,
      "city" -> text,
      "phoneNumber" -> text,
      "idno" -> longNumber
    )(models.formdata.UserProfileForm.apply)(models.formdata.UserProfileForm.unapply)
  )
*/

  // text.verifying("Inte ett unikt användarnamn", txt=>isNew(txt))

  // Forms
  val AnvandareForm = Form(
    mapping(
      "name" -> nonEmptyText,
      "name2" -> text,
      "aboutmeheadline" -> text,
      "aboutme" -> text,
      "county" -> text,
      "streetAddress" -> text,
      "zipCode" -> text,
      "city" -> text,
      "phoneNumber" -> text,
      "personnummer" -> text.verifying(Messages("profile.personalidentitynumber.unique"), { g => isCorrectIdentificationNumber(g)} ),
      "acceptTerms" -> boolean.verifying(Messages("profile.approve.memberterms"), h => h),
      "mainimage" -> optional(text),
      "avatarimage" -> optional(text),
      "firstName" -> nonEmptyText,
      "lastName" -> nonEmptyText,
      "emailAddress" -> nonEmptyText,
      "emailAddress2" -> text
    )(UserProfileDataForm.apply)(UserProfileDataForm.unapply)
      verifying(Messages("profile.create.form.emailAddress.uniq"), e => isUniqueEmailAddress(e.emailAddress, e.emailAddress2))
      verifying(Messages("profile.control.unique"), f => isUniqueProfileName(f.name, f.name2))
  )

  val OptionsForm = Form(
    mapping(
      "payCash" -> boolean,
      "paySwish" -> boolean,
      "payBankCard" -> boolean,
      "payIZettle" -> boolean,
      "wantsToBeHost" -> boolean,
      "tagList" -> optional(list[TagCheckboxForm]{
        mapping(
        "value" -> text
        )(TagCheckboxForm.apply)(TagCheckboxForm.unapply)
      })
    )(UserProfileOptionsForm.apply)(UserProfileOptionsForm.unapply))


  private def isCorrectIdentificationNumber(id: String): Boolean = {
    if (id.length < 1) return true
    if (id.matches("[1-2][0-9]{11}")) return true
    if (id.matches("[0-9]{6}[-][0-9]{4}")) return true
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
        }else{
          false
        }
      }
    }
  }

  private def isThisMyProfile(profile: UserProfile)(implicit request: RequestWithUser[AnyContent,UserCredential]): Boolean = {
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
        }else{
          false
        }
      }
    }
  }


  def verifyUserProfile = SecuredAction(authorize = WithRole(RoleEnums.USER)) { implicit request: SecuredRequest[AnyContent,UserCredential] =>
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
        val profileOwner = profile.getOwner

        val myProfile = isThisMyProfile(profile)
        val recipeBoxes = recipeService.getRecipeBoxes(profileOwner)
        val eventBoxes = eventService.getEventBoxes(profileOwner)
        val bookingsMadeByMe = if (myProfile) eventService.getBookingsMadeByMe(profileOwner, this.getBaseUrl) else None
        val bookingsMadeToMyEvents = if (myProfile) eventService.getBookingsMadeToMyEvents(profileOwner, this.getBaseUrl) else None
        val myReviewBoxes = if (myProfile) ratingService.getMyUserReviews(profileOwner) else None
        val myRecipeReviewBoxes = if (myProfile) ratingService.getMyUserReviewsAboutFood(profileOwner) else None
        val reviewBoxesAboutMyFood = ratingService.getUserReviewsAboutMyFood(profileOwner)
        val reviewBoxesAboutMe = ratingService.getUserReviewsAboutMe(profileOwner)
        val tags = tagWordService.findByProfileAndGroup(profile, "profile")
        val messages = if (myProfile) buildMessageList(profileOwner) else None
        val metaData = buildMetaData(profile, request)
        val shareUrl = createShareUrl(profile)
        val userRateForm = ratingController.renderUserRateForm(profileOwner, routes.UserProfileController.viewProfileByName(profile.profileLinkName).url, request.user)
        val userLikeForm = likeController.renderUserLikeForm(profileOwner, request.user)
        val requestForm = messagesController.renderHostForm(profileOwner, request.user)
        val favorites = favoritesController.renderFavorites(profile)

        // Should the event be registered or not
        val doCountEvent: Boolean = true

        if (doCountEvent) {
          doDebugLogViewOfUserProfile(request, profile)
          doLogViewOfUserProfile(request, profile)
        }


        Ok(views.html.profile.index(
          userProfile = profile,
          recipeBoxes = recipeBoxes,
          eventBoxes = eventBoxes,
          bookingsMadeByMe = bookingsMadeByMe,
          bookingsMadeToMyEvents = bookingsMadeToMyEvents,
          myReviewBoxes = myReviewBoxes,
          myRecipeReviewBoxes = myRecipeReviewBoxes,
          reviewBoxesAboutMyFood = reviewBoxesAboutMyFood,
          reviewBoxesAboutMe = reviewBoxesAboutMe,
          userMessages = messages,
          tagList = tags,
          metaData = metaData,
          shareUrl = shareUrl,
          isThisMyProfile = myProfile,
          currentUser = request.user,
          userRateForm = userRateForm,
          userLikeForm = userLikeForm,
          requestForm = requestForm,
          favorites = favorites
        ))
      case None =>
        val errMess = "Cannot find user profile using name:" + profileName
        Logger.debug(errMess)
        //NotFound(views.html.error.notfound(refUrl = request.path)(request, request2Messages))
        NotFound(views.html.error.notfound(refUrl = request.path))
    }
  }

  def doDebugLogViewOfUserProfile(request: RequestWithUser[AnyContent,UserCredential], profile: UserProfile) {

    if(!environment.mode.equals(Mode.Prod)) {
      val viewer: Option[UserProfile] = request.user match {
        case Some(user) => Some(user.profiles.asScala.head)
        case None => None
      }

      if (viewer.isEmpty) {
        Logger.debug("Log access to userprofile : " + profile.getOwner.getFullName + "viewer: " + "Anonymouse viewer")
      }
      else {
        Logger.debug("Log access to userprofile : " + profile.getOwner.getFullName + "viewer : " + viewer.get.profileLinkName)
      }
    }

  }

  /**
   * Log the access to a user profile member
   * A loged on member or an unkown user accessing a profile
   * @param request the users requesting viewing the profile
   * @param profile the user of the profile page
   */
  def doLogViewOfUserProfile(request: RequestWithUser[AnyContent,UserCredential], profile: UserProfile) {

    // Kontrollera att det är en inloggad användare
    if (request.user.isEmpty) {

      // Det är inte en inloggad användare

      val ipAddress = request.remoteAddress

      // Eftersom vi inte har något användarnamn får vi hämta ip-adress
      // Kontrollera om profilen dvs. den visade sisan har något objekt för att spara
      // undan icke inloggade användare
      if (profile.getUnKnownVisited != null && profile.getUnKnownVisited != None && profile.getUnKnownVisited.getSize() > 0) {

       // println("1 ... ")
       // println("Acess to a ready userProfile for saving UnKnownVisits ... ")

        var log = profile.getUnKnownVisited
        var util: ViewedByUnKnownUtil = new ViewedByUnKnownUtil()


        var itr   =  profile.getUnKnownVisited.getIterator()

        while(itr.hasNext) {

          var s : String = itr.next()
        //  println("v: " + s)
        }




        // Number of days to store data as invidual logposts
        var oldestDate: Date = util.xDayEarlier(1)

        // Remove older recorded data and count number of accesss to the profile page
        //util.removeAllAccessOlderThen(oldestDate, log)

        //remove the sam ip-address if it is stored before
        util.removeOldAccessOfSameHost(ipAddress, log)

        // save access of profile to the profile users node for ...
        userProfileService.logUnKnownProfileViewByObjectId(log, ipAddress)
      } else {

       // println("2 ... ")

        // Det finns inget objekt med icke inloggade användare
        // skapa objektet
        var viewedByUnKnown = new ViewedByUnKnown()
        var util: ViewedByUnKnownUtil = new ViewedByUnKnownUtil()

        // Skapa ett besök av en okänd användare
        viewedByUnKnown.viewedBy(ipAddress, util.getNowString)
        profile.setViewedByUnKnown(viewedByUnKnown)
        userProfileService.saveUserProfile(profile)

      }

    } else {

      // inloggad användare försöker se på en profilsida.....

      // fetch logged in user
      val theUser: Option[UserProfile] = request.user match {
        case Some(user) => Some(user.profiles.asScala.head)
        case None => None
      }

      val vOId: String = theUser match {
        case Some(v) => v.objectId.toString
        case None => ""
      }

      // Member access
      if (profile.getmemberVisited() != null && profile.getmemberVisited != None && profile.getmemberVisited.getSize > 0) {
        var log = profile.getmemberVisited()

       // println("3 ...")

        var itr   =  profile.getmemberVisited.getIterator()

        while(itr.hasNext) {

          var s : String = itr.next()
       //   println("vx: " + s)
        }



        var util: ViewedByMemberUtil = new ViewedByMemberUtil()
        profile.setViewedByMeber(log)

        // Number of days to store data as invidual logposts
        var oldestDate: Date = util.xDayEarlier(7)

        // Remove older recorded data and count number of accesss to the profile page
        //util.removeAllAccessOlderThen(oldestDate, log)

        // remove the same member if it have been viewn the same profile an other date
        util.removeOldAccessOfSameUser(profile.objectId.toString, log)

        // save access of profile to the profile users node for ...
        userProfileService.logProfileViewByObjectId(log, vOId, profile.objectId.toString)
      } else {

       // println("4 ...")

        // Om objektet inte finns lägger den till detta...
        var log = new ViewedByMember()
        var util: ViewedByMemberUtil = new ViewedByMemberUtil()


        userProfileService.logProfileViewByObjectId(log, vOId, profile.objectId.toString)
        profile.setViewedByMeber(log)
        userProfileService.saveUserProfile(profile)
      }

    }
  }


  def viewProfileByLoggedInUser = SecuredAction(authorize = WithRole(RoleEnums.USER)) { implicit request: SecuredRequest[AnyContent,UserCredential] =>

    userProfileService.findByowner(request.user) match {
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
   Show userProfile for edit
   display profile data for the current user to be changed
   my profile
    * ***************************************************************************************************/
  def edit = SecuredAction(authorize = WithRole(RoleEnums.USER)) { implicit request: SecuredRequest[AnyContent,UserCredential] =>

    val userProfile = request.user.getUserProfile
    val identificationNumber = userProfile.getOwner.personNummer

    // Get HOST or not
    var isUserHost: Boolean = false
    if (userProfile.getRole.contains(UserLevelScala.HOST.Constant)) {
      isUserHost = true
    }

    // Tags
    val sortedTagList = getSortedTagWordList
    val userProfOldSavedTags = Option(userProfile.getTags.asScala.toList.map{ tr => tr.tagWord})

    // County
    val countyObjectId:String = userProfile.getLocations.asScala match {
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


    // Fill forms with stored values
    val newForm = AnvandareForm.fill(UserProfileDataForm(
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
      userProfile.getOwner.emailAddress
    ))

    val newOptForm = OptionsForm.fill(UserProfileOptionsForm(
      userProfile.payCash,
      userProfile.paySwish,
      userProfile.payBankCard,
      userProfile.payIZettle,
      isUserHost,
      remapListWithTagWordsToForm(userProfOldSavedTags)
    ))

    // Other values not fit to be in form-classes
    val extraValues = setExtraValues(userProfile)

    Ok(views.html.profile.editProfile(
      newForm,
      newOptForm,
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
        t:TagWord =>
          TagCheckboxForm(t.objectId.toString)
      })
    }
  }

  /*
  private def buildSortedTagListForForm(userProfile: UserProfile): List[TagCheckboxForm] = {
    // Fetch all tags & mark previously selected
    val allTags = tagWordService.listByGroupOption("profile")
    val userSelectedTags = userProfile.getTags.asScala
    val tagList = new mutable.HashSet[TagCheckboxForm]()

    if (allTags.isDefined) {
      for ((theTag, i) <- allTags.get.zipWithIndex) {
        val isSelected = userSelectedTags.exists(x => x.tagWord.tagId == theTag.tagId)
        tagList.add(TagCheckboxForm(theTag.tagName, theTag.objectId.toString, isSelected))
      }
    }

    // Sort & List
    tagList.toList.sortBy(tw => tw.name)
  }
*/

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

/*
  /** ******************************************************
    * Show intrests
    * @return
    */

  def showTags = SecuredAction { implicit request =>

    // Fetch UserProfile from UserCredentials that is fetch by SocialSocial
    var theUser = request.user.asInstanceOf[UserCredential].profiles.iterator().next()

    // Pre selected
    val typ = new Types
    var userTags = theUser.getTags

    if (userTags != null) {
      var itterTags = userTags.iterator

      while (itterTags.hasNext) {
        typ.addVald(itterTags.next().tagWord.tagName)
      }
    }

    // Fetch all tags
    var d = tagWordService.listByGroupOption("profile")
    var l: Long = 0
    var tagList: mutable.HashSet[Type] = new mutable.HashSet[Type]()
    if (d.isDefined) {
      for (theTag <- d.get) {
        var newType: Type = new Type(l, theTag.tagName, theTag.tagName, "quality[" + l + "]")
        l = l + 1

        tagList.add(newType)
      }
    }

    // Sort & List
    val retTagList = tagList.toList.sortBy(tw => tw.name)


    // File with stored values
    val tData: TagsData = new TagsData(
      List("adam", "bertil")
    )

    val nyForm = tagForm.fill(tData)

    Ok(views.html.profile.tags(tagForm, retTagList, typ))
  }
*/

  // add favorite
  def addFavorite(userCredentialObjectId: String) = SecuredAction() { implicit request: SecuredRequest[AnyContent,UserCredential] =>
    val friendsUserCredential = userCredentialService.findById(UUID.fromString(userCredentialObjectId))
    userProfileService.addFavorites(request.user.getUserProfile, friendsUserCredential.get)

    Ok("Ok")
  }

  // remove favorite
  def removeFavorite(userCredentialObjectId: String) = SecuredAction() { implicit request: SecuredRequest[AnyContent,UserCredential] =>
    val friendsUserCredential = userCredentialService.findById(UUID.fromString(userCredentialObjectId))
    userProfileService.removeFavorites(request.user.getUserProfile, friendsUserCredential.get)

    Ok("Ok")
  }


  // remove favorite
  def isFavoriteToMe(userCredentialObjectId: String) = UserAwareAction() { implicit request =>

    var user = request.user
    var isLoggedIn = false
    var svar = ""
    var errorOcurs = false
    var execAnwer = false

    var hasAccess = user match {
      case Some(user) => true
      case None => false
    }

    var theUser: Option[models.UserProfile] = user match {
      case Some(user) => Some(user.profiles.asScala.head)
      case None => None
    }



    // var theUser = request.user.asInstanceOf[UserCredential].profiles.asScala.head

    if (hasAccess) {

      var uuid: UUID = UUID.fromString(userCredentialObjectId)
      var friendsUserCredential = userCredentialService.findById(uuid)

      var isToSerach = friendsUserCredential match {
        case Some(friendsUserCredential) => true
        case None => false
        case _ => false
      }




      if (isToSerach) {

        try {
          execAnwer = userProfileService.isFavoritesToMe(theUser.get, friendsUserCredential.get)
        } catch {
          case e: Exception => execAnwer = false
        }


        svar = execAnwer match {
          case true => "YES"
          case false => "NO"
        }
      } else {
        svar = "NO"
      }

    } else {
      svar = "USER_NOT_LOGGED_IN"
    }



    Ok(svar)
  }



  def showFavoritesPage = SecuredAction { implicit request =>
    Ok(views.html.profile.addAsFavorite(request.user.getUserProfile))
  }

/*
  /**
   * Save chaged tages
   * @return
   */
  def saveTags = SecuredAction { implicit request =>

    var map: Map[String, String] = Map()


    tagForm.bindFromRequest.fold(
      errors => {
        if (errors.hasErrors) {
          Ok("Tags kunde inte sparas")
        }
      },
      reqUserProfile => {
        // test

        for (d <- reqUserProfile.quality) {
          map += (d -> d)

        }


      })

    var theUser = request.user.asInstanceOf[UserCredential].profiles.asScala.head
    var d = tagWordService.listByGroupOption("profile")
    userProfileService.updateUserProfileTags(theUser, d, map)

    Redirect(routes.UserProfileController.showTags())
  }
*/

  /*
  def convYesToTrueElseToFalse(arg: String): Boolean = arg match {
    case "" => false
    case "JA" => true
    case "YES" => true
    case "Yes" => true
    case "Ja" => true
    case _ => false
  }


  def convOptionStringToString(arg: Option[String]): String = arg match {
    case Some(arg) => arg
    case _ => ""
  }


  def convBooleanTOYesOrNo(arg: Boolean): String = arg match {
    case true => "JA"
    case false => "NEJ"
  }
*/

  /** **************************************************************************************************
    Save UserProfile
    * **************************************************************************************************/
  def editSubmit = SecuredAction(authorize = WithRole(RoleEnums.USER))(parse.multipartFormData) { implicit request =>

    // My UserProfile
    var userCredential = request.user
    var userProfile = userCredential.getUserProfile

    var allTagsSelectedInForm: Option[List[TagCheckboxForm]] = None
    //var tagsToSave: List[TagWord] = Nil

    /*
    var aboutMeHeadlineText: String = ""
    var aboutMeText: String = ""
    var profileLinkName: String = ""
    var zipCode: String = ""
    var streetAddress: String = ""
    var city: String = ""
    var phoneNumber: String = ""
    */
    var countyId: String = ""
    var acceptTerms: Boolean = false
    var payBankCard: Boolean = false
    var payCash: Boolean = false
    var payIZettle: Boolean = false
    var paySwish: Boolean = false
    var wantsToBeHost: Boolean = false
    var emailAddress: String = ""

    // Fetch opt-form values
    val optForm = OptionsForm.bindFromRequest.fold(
      error => {
        Logger.error("Error cannot read OptionsForms")
        error
      },
      formValues => {
        payCash = formValues.payCash
        payBankCard = formValues.payBankCard
        payIZettle = formValues.payIZettle
        paySwish = formValues.paySwish

        wantsToBeHost = formValues.wantsToBeHost
        allTagsSelectedInForm = formValues.tagList

        OptionsForm.fill(UserProfileOptionsForm(
          payCash,
          paySwish,
          payBankCard,
          payIZettle,
          wantsToBeHost,
          allTagsSelectedInForm)
        )
      }
    )

    // Fetch main-form values
    AnvandareForm.bindFromRequest.fold(
      errors => {

        // Fetch all tags
        val sortedTagList = getSortedTagWordList

        // Other values not fit to be in form-classes
        val extraValues = setExtraValues(userProfile)

        BadRequest(views.html.profile.editProfile(
          profileDataForm = errors,
          profileOptsForm = optForm,
          listOfTags = sortedTagList,
          extraValues = extraValues,
          optionsLocationAreas = countyService.getCounties,
          termsAndConditions = contentService.getTermsAndConditions))
      },
      formValues => {
        emailAddress = formValues.emailAddress

        // The users are always guests
        if (!userProfile.getRole.contains(UserLevelScala.GUEST.Constant)) {
          userProfile.getRole.add(UserLevelScala.GUEST.Constant)
        }

        // Add user to Host role
        wantsToBeHost match {
          case true =>
            if (!userProfile.getRole.contains(UserLevelScala.HOST.Constant)) {
              userProfile.getRole.add(UserLevelScala.HOST.Constant)
            }
          case false => {
            userProfile.getRole.remove(UserLevelScala.HOST.Constant)
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
    routes.StartPageController.index().absoluteURL(secure = false).dropRight(1)
  }

}

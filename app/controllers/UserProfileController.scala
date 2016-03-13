package controllers

import java.util
import java.util.{Date, UUID}
import javax.inject.{Named, Inject}
import constants.FlashMsgConstants
import enums.RoleEnums
import models.formdata.UserProfileOptions
import models.modelconstants.UserLevelScala
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
                                       val messageService: MessageService,
                                       val messagesApi: MessagesApi) extends Controller with SecureSocial with I18nSupport {

/*
  // Services
  @Autowired
  var userProfileService: UserProfileService = _

  @Autowired
  var tagWordService: TagWordService = _

  @Autowired
  var contentService: ContentService = _

  @Autowired
  var countyService: CountyService = _

  @Autowired
  private var recipeService: RecipeService = _

  @Autowired
  private var eventService: EventService = _

  @Autowired
  private var ratingService: RatingService = _

  @Autowired
  private var fileService: ContentFileService = _

  @Autowired
  var userCredentialService: UserCredentialService = _

  @Autowired
  var messageService: MessageService = _
*/


  // Constants
  val FOOD = "food-tab"
  val BLOG = "blog-tab"
  val REVIEWS = "reviews-tab"
  val INBOX = "inbox-tab"
  val FAVOURITES = "favourites-tab"
  val EVENT = "event-tab"

  val tabMenu: TabMenu = TabMenu(FOOD, BLOG, REVIEWS, INBOX, FAVOURITES, EVENT)

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


  // text.verifying("Inte ett unikt användarnamn", txt=>isNew(txt))

  val AnvandareForm = Form(
    mapping(
      "name" -> text,
      "name2" -> text,
      "aboutmeheadline" -> text,
      "aboutme" -> text,
      "county" -> text,
      "streetAddress" -> text,
      "zipCode" -> text,
      "city" -> text,
      "phoneNumber" -> text,
      "personnummer" -> text,
      "acceptTerms" -> boolean,
      //"childFfriendly" -> optional(text),
      //"handicapFriendly" -> optional(text),
      //"havePets" -> optional(text),
      // "smoke" -> optional(text),
      "allkoholServing" -> optional(text),
      "mainimage" -> optional(text),
      "avatarimage" -> optional(text),
      "firstName" -> text,
      "lastName" -> text,
      "emailAddress" -> text,
      "emailAddress2" -> text
    )(EnvData.apply)(EnvData.unapply)
      verifying(Messages("profile.create.form.emailAddress.uniq"), e => isUniqueEmailAddress(e.emailAddress, e.emailAddress2))
      verifying(Messages("profile.control.unique"), f => isUniqueProfileName(f.name, f.name2))
      verifying(Messages("profile.personalidentitynumber.unique"), g => isCorrectPersonnummer(g.personnummer))
      verifying(Messages("profile.approve.memberterms"), h => h.acceptTerms == true)
  )


  val OptionsForm = Form(
    mapping(
      "payCache" -> optional(text),
      "paySwish" -> optional(text),
      "payBankCard" -> optional(text),
      "payIZettle" -> optional(text),
      //"roleGuest" -> optional(text),
      "roleHost" -> optional(text),
      "maxGuest" -> text,
      "minGuest" -> text,
      "quality" -> list(text),
      "handicapFriendly" -> optional(text),
      "childFfriendly" -> optional(text),
      "havePets" -> optional(text),
      "smoke" -> optional(text)
    )
      (UserProfileOptions.apply)(UserProfileOptions.unapply))

  val tagForm = Form(
    mapping(
      "quality" -> list(text)
    )
      (TagsData.apply)(TagsData.unapply)
  )

  def isCorrectPersonnummer(personnummer: String): Boolean = {
    if (personnummer.size < 1) return true

    if (personnummer.matches("[1-2][0-9]{11}")) return true
    if (personnummer.matches("[0-9]{6}[-][0-9]{4}")) return true


    false
  }

  def isUniqueProfileName(profileName: String, storedProfileName: String): Boolean = {

    // Länknamn måste börja på en bokstav, stor eller liten
    // därefter kan det koimma en stor eller liten bokstav elln siffra eller ett bindesträck
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

  def isUniqueEmailAddress(emailAddress: String, storedEmailAddress: String): Boolean = {

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

        // should the event be registred or not
        val doCountEvent: Boolean = true

        if (doCountEvent) {

          val viewer: Option[UserProfile] = request.user match {
            case Some(user) => Some(user.profiles.asScala.head)
            case None => None
          }

          if (viewer == None) {
            println("Log access to userprofile : " + profile.getOwner().getFullName + "viwer: " + "Annonymouse viewer")
          }
          else {
          println("Log access to userprofile : " + profile.getOwner().getFullName + "viwer : " +  viewer.get.profileLinkName)
        }

          doLogViewOfUserProfile(request, profile)


        } // false


        Ok(views.html.profile.index(
          userProfile = profile,
          tabMenu = tabMenu,
          recipeBoxes = recipeBoxes,
          eventBoxes = eventBoxes,
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


    // Fetch UserProfile from UserCredentials
    var userProfile = request.user.getUserProfile
    var personnummer = userProfile.getOwner.personNummer
    var countiesList = countyService.getCounties

    var countyItter = countiesList.iterator
    while (countyItter.hasNext) {
      var nextCo = countyItter.next()
    }

    //  Test
    var host: String = ""
    var guest: String = ""

    if (userProfile.getRole.contains(UserLevelScala.HOST.Constant)) {
      host = UserLevelScala.HOST.Constant
    }
    if (userProfile.getRole.contains(UserLevelScala.GUEST.Constant)) {
      guest = UserLevelScala.GUEST.Constant
    }

    val provider = request.user.providerId
    val user = request.user.userId


    // show profile

    // 1. Get the correct profile

    // 2. Load the tags

    var locationId: String = ""


    // Pre selected
    val typ = new models.Types

    var userTags = userProfile.getTags

    if (userTags != null) {
      var itterTags = userTags.iterator

      while (itterTags.hasNext) {
        typ.addVald(itterTags.next().tagWord.tagName)
      }
    }

    // Fetch all tags
    var d = tagWordService.listByGroupOption("profile")


    var l: Long = 0
    var tagList: mutable.HashSet[models.Type] = new mutable.HashSet[models.Type]()



    if (d.isDefined) {
      for (theTag <- d.get) {
        var newType: models.Type = new models.Type(l, theTag.tagName, theTag.tagName, "quality[" + l + "]")
        l = l + 1

        tagList.add(newType)
      }
    }

    // Sort & List
    val retTagList = tagList.toList.sortBy(tw => tw.name)


    try {
      locationId = userProfile.getLocations.iterator.next().county.objectId.toString
      //locationId = "0ec35cae-495c-43b8-b99c-bc14755288f2"
    } catch {
      case e: Exception => println("COUNTY EXCEPTION : " + e.getMessage)
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


    // File with stored values
    val eData: EnvData = new EnvData(
      userProfile.profileLinkName,
      userProfile.profileLinkName,
      userProfile.aboutMeHeadline,
      userProfile.aboutMe,
      locationId, // county
      userProfile.streetAddress, // street Address,
      userProfile.zipCode, // zip code
      userProfile.city, // city
      userProfile.phoneNumber, // phone number
      personnummer, // TODO
      userProfile.isTermsOfUseApprovedAccepted, // isTermsOfUseApprovedAccepted
      // Option(theUser.childFfriendly),
      // Option(theUser.handicapFriendly),
      // Option(theUser.havePets),
      //Option(theUser.smoke),
      Option(userProfile.allkoholServing),
      mainImage,
      avatarImage,
      userProfile.getOwner.firstName,
      userProfile.getOwner.lastName,
      userProfile.getOwner.emailAddress,
      userProfile.getOwner.emailAddress
    )



    val uOptValues = new UserProfileOptValues(
      safeJava(userProfile.payCache),
      safeJava(userProfile.paySwish),
      safeJava(userProfile.payBankCard),
      safeJava(userProfile.payIZettle),
      safeJava(guest),
      safeJava(host),
      safeJava(userProfile.maxNoOfGuest),
      safeJava(userProfile.minNoOfGuest),
      safeJava(userProfile.handicapFriendly), // moved from EnvData.
      safeJava(userProfile.childFfriendly),
      safeJava(userProfile.havePets),
      safeJava(userProfile.smoke)
    )

    // Other values not fit to be in form-classes
    val extraValues = setExtraValues(userProfile)


    val nyForm = AnvandareForm.fill(eData)
    Ok(views.html.profile.editProfile(nyForm, uOptValues,
      retTagList, typ,
      optionsLocationAreas = countyService.getCounties,
      extraValues = extraValues,
      editingProfile = Some(userProfile),
      termsAndConditions = contentService.getTermsAndConditions)
    )

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

  def safeJava(value: String): String = {
    var outString: String = ""

    if (value != null && value != None) {
      outString = value
    }

    outString
  }


  /** ******************************************************
    * Show intrests
    * @return
    */

  def showTags = SecuredAction { implicit request =>

    // Fetch UserProfile from UserCredentials that is fetch by SocialSocial
    var theUser = request.user.asInstanceOf[UserCredential].profiles.iterator().next()

    // Pre selected
    val typ = new models.Types

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
    var tagList: mutable.HashSet[models.Type] = new mutable.HashSet[models.Type]()



    if (d.isDefined) {
      for (theTag <- d.get) {
        var newType: models.Type = new models.Type(l, theTag.tagName, theTag.tagName, "quality[" + l + "]")
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


  // add favorite
  def addFavorite(userCredentialObjectId: String) = SecuredAction() { implicit request: SecuredRequest[AnyContent,UserCredential] =>
    var theUser = request.user.profiles.asScala.head

    var uuid: UUID = UUID.fromString(userCredentialObjectId)
    var friendsUserCredential = userCredentialService.findById(uuid)
    userProfileService.addFavorites(theUser, friendsUserCredential.get)

    Ok("Ok")
  }

  // remove favorite
  def removeFavorite(userCredentialObjectId: String) = SecuredAction() { implicit request: SecuredRequest[AnyContent,UserCredential] =>
    var theUser = request.user.profiles.asScala.head

    var uuid: UUID = UUID.fromString(userCredentialObjectId)
    var friendsUserCredential = userCredentialService.findById(uuid)

    // todo calll new method to remove favorite

    userProfileService.removeFavorites(theUser, friendsUserCredential.get)

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


  def testAction = Action { implicit request =>

    var host = request.remoteAddress


    var keys = request.headers.keys.iterator
    var str: String = ""

    while (keys.hasNext) {
      var obj = keys.next()
      str = str + "\n key = " + obj + " value= " + request.headers.apply(obj)
    }

    str = str + "\n Remote host : " + host

    Ok("Datum: " + DateTime.now() + str)
  }

  // SecuredAction
  // UserAwareAction
  // Action
  def testAction2(callingString: String) = UserAwareAction() { implicit request =>

    val user: Option[UserCredential] = request.user
    var response = ""

    val hasAccess = user match {
      case Some(user) => true
      case None => false
    }

    val loggedIdUserProfile: Option[models.UserProfile] = user match {
      case Some(user) => Some(user.profiles.asScala.head)
      case None => None
    }

    if (hasAccess == true) {

      response = loggedIdUserProfile.get.profileLinkName
    } else {
      response = "NO_USER_LOGGED_IN"
    }

    Ok(response)
  }

  def showFavoritesPage = SecuredAction { implicit request =>

    var userProfile = request.user.asInstanceOf[UserCredential].profiles.asScala.head


    Ok(views.html.profile.addAsFavorite(userProfile))
  }


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


  /** **************************************************************************************************
    Save UserProfile
    * **************************************************************************************************/
  def editSubmit = SecuredAction(authorize = WithRole(RoleEnums.USER))(parse.multipartFormData) { implicit request =>

    // My UserProfile
    var userCredential = request.user
    var theUserProfile = userCredential.getUserProfile

    val typ = new models.Types // all tags selected
    var map: Map[String, String] = Map() // to update tags in user profile

    var aboutMeHeadlineText: String = ""
    var aboutMeText: String = ""
    var profileLinkName: String = ""
    var zipCode: String = ""
    var streetAddress: String = ""
    var city: String = ""
    var phoneNumber: String = ""
    var countyId: String = ""
    var acceptTerms: Boolean = false

    var childFfriendly: String = ""
    var handicapFriendly: String = ""
    var havePets: String = ""
    var smoke: String = ""
    var allkoholServing: String = ""

    var payBankCard: String = ""
    var payCache: String = ""
    var payIZettle: String = ""
    var paySwish: String = ""
    var roleGuest: String = ""
    var roleHost: String = ""
    var numberOfGuest: String = ""
    var minGuest: String = ""

    var firstName: String = ""
    var lastName: String = ""

    var emailAddress: String = ""

    OptionsForm.bindFromRequest.fold(
      error => println("Error reading options "),
      ok => {
        payBankCard = ok.payBankCard.getOrElse("")
        payCache = ok.payCache.getOrElse("")
        payIZettle = ok.payIZettle.getOrElse("")
        paySwish = ok.paySwish.getOrElse("")

        roleHost = ok.roleHost.getOrElse("")
        numberOfGuest = ok.maxGuest
        minGuest = ok.minGuest

        handicapFriendly = ok.handicapFriendly.getOrElse("")
        childFfriendly = ok.childFfriendly.getOrElse("")
        havePets = ok.havePets.getOrElse("")
        smoke = ok.smoke.getOrElse("")

        for (tags <- ok.quality) {
          typ.addVald(tags)
          map += (tags -> tags)
        }
      }
    )


    val uOptValues = new UserProfileOptValues(
      payCache, paySwish,
      payBankCard, payIZettle, roleGuest, roleHost,
      numberOfGuest, minGuest, handicapFriendly, childFfriendly, havePets, smoke)

    // Handle tags from form ...
    // Fetch all tags
    var d = tagWordService.listByGroupOption("profile")
    var l: Long = 0
    var tagList: mutable.HashSet[models.Type] = new mutable.HashSet[models.Type]()

    if (d.isDefined) {
      for (theTag <- d.get) {
        var newType: models.Type = new models.Type(l, theTag.tagName, theTag.tagName, "quality[" + l + "]")
        l = l + 1

        tagList.add(newType)
      }
    }
    // Sort & List
    val retTagList = tagList.toList.sortBy(tw => tw.name)

    // Other values not fit to be in form-classes
    val extraValues = setExtraValues(theUserProfile)

    // Handle tags end ...
    AnvandareForm.bindFromRequest.fold(
      errors => {
        BadRequest(views.html.profile.editProfile(errors,
          uOptValues,
          retTagList, typ,
          extraValues = extraValues,
          optionsLocationAreas = countyService.getCounties,
          termsAndConditions = contentService.getTermsAndConditions))

      },
      reqUserProfile => {

        aboutMeHeadlineText = reqUserProfile.aboutmeheadline
        aboutMeText = reqUserProfile.aboutme
        profileLinkName = reqUserProfile.name
        zipCode = reqUserProfile.zipCode
        streetAddress = reqUserProfile.streetAddress
        city = reqUserProfile.city
        phoneNumber = reqUserProfile.phoneNumber
        countyId = reqUserProfile.county
        acceptTerms = reqUserProfile.acceptTerms
        firstName = reqUserProfile.firstName
        lastName = reqUserProfile.lastName
        allkoholServing = convOptionStringToString(reqUserProfile.allkoholServing)

        // Gäst och värd
        // The user are always guest
        if (!theUserProfile.getRole.contains(UserLevelScala.GUEST.Constant)) theUserProfile.getRole.add(UserLevelScala.GUEST.Constant)

        uOptValues.isBooleanSelectedHost match {
          case true =>
            if (!theUserProfile.getRole.contains(UserLevelScala.HOST.Constant)) theUserProfile.getRole.add(UserLevelScala.HOST.Constant)
          case false =>
            theUserProfile.getRole.remove(UserLevelScala.HOST.Constant)
        }


        // save: UserCredential
        userCredential.personNummer = reqUserProfile.personnummer
        userCredential.firstName = firstName
        userCredential.lastName = lastName
        userCredential.fullName = firstName + " " + lastName

        emailAddress = reqUserProfile.emailAddress

        userCredential.userId = emailAddress
        userCredential.emailAddress = emailAddress

        Logger.debug("userCredentials.userId : " + userCredential.userId)
        Logger.debug("userCredentials.emailAddress : " + userCredential.emailAddress)

        var t = userCredentialService.save(userCredential)

        //theUserProfile.firstName = firstName
        //theUserProfile.lastName = lastName

        theUserProfile.aboutMeHeadline = aboutMeHeadlineText
        theUserProfile.aboutMe = aboutMeText
        theUserProfile.profileLinkName = profileLinkName
        theUserProfile.city = city
        theUserProfile.zipCode = zipCode
        theUserProfile.streetAddress = streetAddress
        theUserProfile.phoneNumber = phoneNumber

        theUserProfile.childFfriendly = childFfriendly
        theUserProfile.handicapFriendly = handicapFriendly
        theUserProfile.havePets = havePets
        theUserProfile.smoke = smoke
        theUserProfile.allkoholServing = allkoholServing
        theUserProfile.isTermsOfUseApprovedAccepted = acceptTerms

        theUserProfile.payBankCard = payBankCard
        theUserProfile.payCache = payCache
        theUserProfile.payIZettle = payIZettle
        theUserProfile.paySwish = paySwish
        theUserProfile.maxNoOfGuest = numberOfGuest
        theUserProfile.minNoOfGuest = uOptValues.minGuest


        if (countyId == None || countyId == null || countyId.trim().size < 2) {
          //theUser.removeLocation()
          theUserProfile = userProfileService.removeAllLocationTags(theUserProfile)
        } else {
          countyService.findById(UUID.fromString(countyId)) match {
            case None => // Do something when nothing found

              theUserProfile = userProfileService.removeAllLocationTags(theUserProfile)
            case Some(item) =>
              theUserProfile = userProfileService.removeAllLocationTags(theUserProfile)
              theUserProfile = userProfileService.addLocation(theUserProfile, item)

          }
        }

        // Images
        // Avatar image
        reqUserProfile.avatarimage match {
          case Some(imageId) => UUID.fromString(imageId) match {
            case imageUUID: UUID =>
              fileService.getFileByObjectIdAndOwnerId(imageUUID, userCredential.objectId) match {
                case Some(item) => theUserProfile = userProfileService.setAndRemoveAvatarImage(theUserProfile, item)
                case _ => None
              }
          }
          case None => None
        }

        // Main image
        reqUserProfile.mainimage match {
          case Some(imageId) => UUID.fromString(imageId) match {
            case imageUUID: UUID =>
              fileService.getFileByObjectIdAndOwnerId(imageUUID, userCredential.objectId) match {
                case Some(item) => theUserProfile = userProfileService.setAndRemoveMainImage(theUserProfile, item)
                case _ => None
              }
          }
          case None => None
        }

        theUserProfile.keyIdentity = emailAddress + "_userpass"
        theUserProfile.userIdentity = emailAddress

        Logger.debug("UP:userIdentity: " + theUserProfile.userIdentity)
        Logger.debug("UP:keyIdentity: " + theUserProfile.keyIdentity)

        userProfileService.updateUserProfileTags(theUserProfile, d, map)

        Redirect(routes.UserProfileController.edit()).flashing(FlashMsgConstants.Success -> Messages("profile.create.saved-successfully"))
      })
  }

}

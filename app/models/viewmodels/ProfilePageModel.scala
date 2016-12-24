package models.viewmodels

import play.twirl.api.Html

case class ProfilePageModel(userProfile: models.UserProfile,
                            recipeBoxes: Option[List[RecipeBox]] = None,
                            eventBoxes: Option[List[BrowseEventBox]] = None,
                            bookingsMadeByMe: Option[List[EventBookingSuccess]] = None,
                            bookingsMadeToMyEvents: Option[List[EventBookingSuccess]] = None,
                            myReviewBoxes: Option[List[ReviewBox]] = None,
                            myRecipeReviewBoxes: Option[List[ReviewBox]] = None,
                            reviewBoxesAboutMyFood: Option[List[ReviewBox]] = None,
                            reviewBoxesAboutMe: Option[List[ReviewBox]] = None,
                            userMessages: Option[List[ReplyToGuestMessage]] = None,
                            tagList: Option[List[String]] = None,
                            metaData: Option[MetaData] = None,
                            shareUrl: String = "",
                            isThisMyProfile: Boolean = false,
                            currentUser: Option[models.UserCredential],
                            userRateForm: Html = Html(""),
                            userLikeForm: Html = Html(""),
                            requestForm: Html = Html(""),
                            favorites: Option[Html] = None,
                            visMemberCount: Option[Int],
                            visUnknownCount: Option[Int],
                            blogPostsCount: Int,
                            userCredentialAverageRating: Int,
                            userCredentialNrOfTotalRatings: Int) {
}

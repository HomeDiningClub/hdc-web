package controllers

import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import views._
import models._

object UserController extends Controller {

  def index = Action {
    Ok(html.user.index("Users index", userFormParam = this.userForm))
  }

  val userForm = Form(
    tuple(
      "name" -> nonEmptyText,
      "age" -> number(min = 1, max = 140),
      "description" -> text
    )
  )

  def addUser = Action { implicit request =>
    userForm.bindFromRequest.fold(
      formWithErrors => BadRequest(html.user.index("Error occurred")),
      {
        case (name, age, description) =>
          val newUser = User(
            username = name,
            password = "",
            profile = UserProfile(
              description = description,
              age = age,
              country = "",
              address = ""),
            email = "asd"
          )
          Ok(html.user.index("User added", newUser))
      }
    )
  }
}

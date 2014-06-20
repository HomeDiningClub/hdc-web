package controllers

import play.api.data.Form
import play.api.data.Forms.{mapping, longNumber, nonEmptyText}
import play.api.i18n.Messages
import play.api.mvc._
import securesocial.core.{IdentityId, UserService, Identity, Authorization}
import play.api.{Logger, Play}


import play.api.mvc.{Action, Controller}
import models.Person

object Personer extends Controller with securesocial.core.SecureSocial {


  def save = Action {
    implicit request =>
      val nyPersonForm  = personForm.bindFromRequest()

      nyPersonForm.fold(
       hasErrors = {form =>
       Redirect(routes.Personer.ny())
       },
      success = {
        nyPersonForm =>
          Person.add(nyPersonForm)
          Redirect(routes.Personer.list())
         // Redirect(routes.Personer.ny())
      }
      )

  }


  def ny = Action { implicit request =>
    val form = if(flash.get("error").isDefined)
      personForm.bind(flash.data)
    else
      personForm

    Ok(views.html.person.skapa(form))
  }


  def list = Action { implicit request =>
    val personLista = Person.findAll

    Ok(views.html.person.list(personLista))

  }


  def testbild = SecuredAction { implicit request =>

    println("ID: " + request.cookies.get("id"))
    println("SVAR: " + request.cookies.toString())

    var str : String =  ""

    str = str + ", username = " + request.user.identityId.userId

    Ok(views.html.person.testbild())

  }


  private val personForm: Form[Person] = Form(
      mapping(
      "id" -> longNumber,
      "fornamn" -> nonEmptyText,
      "efternamn" -> nonEmptyText
      ) (Person.apply)(Person.unapply)
  )
}
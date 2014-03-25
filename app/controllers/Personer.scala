package controllers

import play.api.data.Form
import play.api.data.Forms.{mapping, longNumber, nonEmptyText}
import play.api.i18n.Messages


import play.api.mvc.{Action, Controller}
import models.Person

object Personer extends Controller {


  def save = Action {
    implicit request =>
      val nyPersonForm  = personForm.bindFromRequest()

      nyPersonForm.fold(
       hasErrors = {form =>
       Redirect(routes.Personer.ny())
       },
      success = {
        nyPersonForm =>
          Redirect(routes.Personer.ny())
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


  private val personForm: Form[Person] = Form(
      mapping(
      "id" -> longNumber,
      "fornamn" -> nonEmptyText,
      "efternamn" -> nonEmptyText
      ) (Person.apply)(Person.unapply)
  )





}
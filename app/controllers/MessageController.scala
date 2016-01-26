package controllers

import javax.inject.{Named, Inject}

import customUtils.security.SecureSocialRuntimeEnvironment
import play.api.data._
import play.api.data.Forms._

import play.api._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import models.viewmodels.{MessageText, Repeat}

//@Named
class MessageController @Inject() (override implicit val env: SecureSocialRuntimeEnvironment,
                                   val messagesApi: MessagesApi) extends Controller with securesocial.core.SecureSocial with I18nSupport {

  val myForm = Form(
    mapping(
      "text" -> text(minLength = 2, maxLength = 4),
      "nods" -> list(text)
    )
      (Repeat.apply) (Repeat.unapply)
  )

  val myForm2 = Form(
    mapping(
      "text" -> text,
      "nods" -> list(text)
    )
      (Repeat.apply) (Repeat.unapply)
  )


  val messageForm = Form(
    "messageinformation" ->
    mapping(
      "text" -> nonEmptyText,
      "kod" -> text(minLength = 1, maxLength = 4)
    )
      (MessageText.apply) (MessageText.unapply)
  )


  def repatit = Action {
    var n : Repeat = Repeat("Moa",List("Apa", "Banan", "Citron", "Dadel", "Ekolon"))
    Ok(views.html.message.show(myForm.fill(n)))
  }

  def saveit = Action { implicit request =>
    myForm.bindFromRequest.fold(
    error => {

      println(error.globalErrors)


      println("save error")
      Ok(views.html.message.show(error))
    },
    values => {
      println("save ok")

      var itter = values.nods.iterator

      //println("Antal : " + itter.size)

      while(itter.hasNext) {
        var textString = itter.next()
        println("TextString : " + textString)
      }


      Ok(views.html.message.show(myForm.fill(values)))
    }
    )
  }



  def show = Action {

    var v : MessageText = MessageText("test", "1")


   Ok(views.html.message.message(messageForm.fill(v)))
  }



  def save = Action { implicit request =>

   messageForm.bindFromRequest.fold(
   errors => {
     println("FEL")
     BadRequest(views.html.message.message(errors))
   }
     ,
   message => {
     println("OK")

     println("1. kod " + message.kod)
     println("2. text " + message.text)

     Ok(views.html.message.message(messageForm.fill(message)))
   }
   )


  }

}

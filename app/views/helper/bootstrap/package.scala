package views.html.helper

import views.html.helper.bootstrap.bootstrapFieldConstructor

package object twitterBootstrap3 {
  implicit val twitterBootstrapField = new FieldConstructor {
    def apply(elts: FieldElements) = bootstrapFieldConstructor(elts)
  }
}
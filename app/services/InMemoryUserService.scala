/**
 * Copyright 2012 Jorge Aliss (jaliss at gmail dot com) - twitter: @jaliss
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package services

import play.api.Logger
import securesocial.core._
import securesocial.core.providers.Token
import securesocial.core.IdentityId
import org.springframework.data.neo4j.support.Neo4jTemplate
import org.springframework.beans.factory.annotation.Autowired
import repositories.UserProfileRepository
import org.springframework.stereotype.{Component, Service}
import play.api.Play.current
import play.api.Application
import org.springframework.context.annotation.Lazy

class InMemoryUserService(application: Application) extends UserServicePlugin (application) {

  @Autowired
  private var template: Neo4jTemplate = _

  @Autowired
  private var userRepository: UserProfileRepository = _


  val logger = Logger("application.controllers.InMemoryUserService")

  // a simple User class that can have multiple identities
  case class User(id: String, identities: List[Identity])

  var users = Map[String, User]()
  //private var identities = Map[String, Identity]()
  private var tokens = Map[String, Token]()

  def find(id: IdentityId): Option[Identity] = {
    if ( logger.isDebugEnabled ) {
      logger.debug("users = %s".format(users))
    }

    // Get from DB
    val userFromDB = userRepository.getUserProfilesByIdentityId(id)

//    val result = for (
//      user <- users.values ;
//      identity <- user.identities.find(_.identityId == id)
//    ) yield {
//      identity
//    }
    //result.headOption

    // Convert to correct type
    val secUser: Option[Identity] = Some(new Identity {
      override def firstName: String = userFromDB.firstName
      override def lastName: String = userFromDB.firstName
      override def fullName: String = userFromDB.identity.fullName
      override def oAuth1Info: Option[OAuth1Info] = None
      override def oAuth2Info: Option[OAuth2Info] = None
      override def avatarUrl: Option[String] = None
      override def passwordInfo: Option[PasswordInfo] = None
      override def authMethod: AuthenticationMethod = userFromDB.identity.authMethod
      override def email: Option[String] = Some(userFromDB.emailAddress)
      override def identityId: IdentityId = userFromDB.identity.identityId
    })

    secUser
  }

  def findByEmailAndProvider(email: String, providerId: String): Option[Identity] = {
    if ( logger.isDebugEnabled ) {
      logger.debug("users = %s".format(users))
    }
    val result = for (
      user <- users.values ;
      identity <- user.identities.find(i => i.identityId.providerId == providerId && i.email.exists(_ == email))
    ) yield {
      identity
    }
    result.headOption
  }

  def save(user: Identity): Identity = {
    // first see if there is a user with this Identity already.
    val maybeUser = users.find {
      case (key, value) if value.identities.exists(_.identityId == user.identityId ) => true
      case _ => false
    }

    maybeUser match {
      case Some(existingUser) =>
        val identities = existingUser._2.identities
        val updated = identities.patch( identities.indexWhere( i => i.identityId == user.identityId ), Seq(user), 1)
        users = users + (existingUser._1 -> User(existingUser._1, updated))
      case _ =>
        val newId = System.currentTimeMillis().toString
        users = users + (newId -> User(newId, List(user)))
    }
    // this sample returns the same user object, but you could return an instance of your own class
    // here as long as it implements the Identity trait. This will allow you to use your own class in the protected
    // actions and event callbacks. The same goes for the find(id: IdentityId) method.
    user
  }

  def link(current: Identity, to: Identity) {
    val currentId = current.identityId.userId + "-" + current.identityId.providerId
    val toId = to.identityId.userId + "-" + to.identityId.providerId
    Logger.info(s"linking $currentId to $toId")

    val maybeUser = users.find {
      case (key, value) if value.identities.exists(_.identityId == current.identityId ) => true
    }

    maybeUser.foreach { u =>
      if ( !u._2.identities.exists(_.identityId == to.identityId)) {
        // do the link only if it's not linked already
        users = users + (u._1 -> User(u._1, to :: u._2.identities  ))
      }
    }
  }

  def save(token: Token) {
    tokens += (token.uuid -> token)
  }

  def findToken(token: String): Option[Token] = {
    tokens.get(token)
  }

  def deleteToken(uuid: String) {
    tokens -= uuid
  }

  def deleteTokens() {
    tokens = Map()
  }

  def deleteExpiredTokens() {
    tokens = tokens.filter(!_._2.isExpired)
  }
}

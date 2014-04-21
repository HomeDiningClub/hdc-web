package services

import org.neo4j.graphalgo.{GraphAlgoFactory}
import org.neo4j.graphdb._
import org.neo4j.helpers.collection.IteratorUtil
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.neo4j.support.Neo4jTemplate
import org.springframework.stereotype.Service
import models.UserProfileData
import models.formdata.UserProfile
import repositories.UserProfileRepository
import scala.collection.mutable.ListBuffer
import scala.collection.JavaConverters._
import scala.List
import scala.language.implicitConversions
import models.relationships.RelationshipTypes
import org.springframework.transaction.annotation.Transactional
import org.springframework.data.neo4j.conversion.EndResult
import org.springframework.data.neo4j.repository.GraphRepository
import org.neo4j.graphdb.index.Index

@Service
class UserProfileService {

  @Autowired
  private var template: Neo4jTemplate = _

  @Autowired
  private var userProfileRepository: UserProfileRepository = _

  @Transactional(readOnly = false)
  def saveUserProfile(userProfile: UserProfileData): UserProfileData = {

    println("ID: " + userProfile.id)
    var modUserProfile = userProfileRepository.save(userProfile)

    modUserProfile
  }




  @Transactional(readOnly = false)
   def saveUserProfile(userName: String, emailAddress: String = ""): UserProfileData = {
    var userProfile: UserProfileData = new UserProfileData(userName, emailAddress)

    userProfile = userProfileRepository.save(userProfile)
    userProfile
  }

  @Transactional(readOnly = true)
  def getAllUserProfiles(): List[UserProfileData] = {
    val listOfUserProfiles: List[UserProfileData] = IteratorUtil.asCollection(userProfileRepository.findAll()).asScala.toList
    listOfUserProfiles
  }





  @Transactional(readOnly = true)
  def getUserProfile(userName : String): UserProfileData = {
    var userProfileDataIndex: Index[Node] = template.getIndex(classOf[UserProfileData], "userName")
    //val node: Node = userProfileDataIndex.query("userName", userName).getSingle()

    var hits = userProfileDataIndex.query("userName", userName)
    var nods = hits.iterator()
    var id : Long = 0

    if(nods.hasNext) {
      id = nods.next().getId
      println("Id: " + id)
    }

    var currNode: UserProfileData = new UserProfileData("","")

    // node != null
    if (id != 0) {
      currNode = userProfileRepository.findOne(id)
    }

    currNode
  }


    @Transactional(readOnly = true)
  def getAllUserProfile(): List[UserProfileData] = {

    // userProfileRepository.finB


    //  var actorRepo : GraphRepository[UserProfileData] =repoFactory.createGraphRepository(UserProfileData);


    //var devs : EndResult[UserProfileData] = userProfileRepository.findAllByPropertyValue("userName", "fredrik")

    //var user : UserProfileData =  devs.single()
    //println("UserName : " + user.emailAddress)


    //userProfileRepository.findByPropertyValue("userName", "fredrik")

    //userProfileRepository.findByPropertyValue("title", "name")

    userProfileRepository.count()

    // var user : UserProfileData = userProfileRepository.getUserProfilesByName("fredrik")
    // println("UserName: " + user.emailAddress)

    var userProfileDataIndex: Index[Node] = template.getIndex(classOf[UserProfileData], "userName")
    val node: Node = userProfileDataIndex.query("userName", "Daniel").getSingle()


    println("Name: " + userProfileDataIndex.getName())



    println("Daniel: " + node.getId())
    println("Daniel: " + node.getProperty("emailAddress"))
    // personFulltextIndex.

    var currNode : UserProfileData = userProfileRepository.findOne(node.getId)

    println("Nod x: " + currNode.emailAddress)

    //personFulltextIndex.getName()

    //userProfileRepository.findA llByQuery("username", "test")

    val listOfUserProfiles: List[UserProfileData] = Nil
    // val listOfUserProfiles: List[UserProfileData] = IteratorUtil.asCollection(userProfileRepository.findAllByQuery("username","fredrik")).asScala.toList
    listOfUserProfiles
  }



}

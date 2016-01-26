import java.util.UUID

//import models.base.AbstractEntity
//import models.content.ContentPage
//import org.neo4j.graphdb.Node
//import org.springframework.context.annotation.{Bean, Configuration}
//import org.springframework.context.ApplicationListener
//import org.springframework.data.neo4j.config.{Neo4jConfiguration, EnableNeo4jRepositories}
//import org.springframework.data.neo4j.lifecycle.BeforeSaveEvent

//@Configuration
//@EnableNeo4jRepositories
//class ApplicationConfig extends Neo4jConfiguration {

//  @Bean
//  def beforeSaveEventApplicationListener(): ApplicationListener[BeforeSaveEvent[Node]] = {
//    return new ApplicationListener[BeforeSaveEvent[Node]]() {
//
//      @Override
//      override def onApplicationEvent(event: BeforeSaveEvent[Node]) {
//        val entity = event.getEntity().asInstanceOf[AbstractEntity]
//        val graphid = entity
//      }
//    }
//  }
//
//  @Bean
//  def beforeSaveEventApplicationListener(): ApplicationListener[BeforeSaveEvent[ContentPage]] = {
//    new ApplicationListener[BeforeSaveEvent[ContentPage]]() {
//
//      override def onApplicationEvent(event: BeforeSaveEvent[ContentPage]) {
//        val entity = event.getEntity
//        //entity.setUniqueId(UUID.randomUUID)
//      }
//    }
//  }
//
//  @Bean
//  def afterSaveEventApplicationListener(): ApplicationListener[AfterSaveEvent] = {
//    new ApplicationListener[AfterSaveEvent]() {
//
//      override def onApplicationEvent(event: AfterSaveEvent) {
//        val entity = event.getEntity.asInstanceOf[AbstractEntity]
//        Logger.debug("Saved entity (" + entity.id + ") objectId(" + entity.objectId + ")")
//      }
//    }
//  }
//
//  @Bean
//  def deleteEventApplicationListener(): ApplicationListener[AfterDeleteEvent] = {
//    new ApplicationListener[AfterDeleteEvent]() {
//
//      override def onApplicationEvent(event: AfterDeleteEvent) {
//        Logger.debug("Deleted entity")
//      }
//    }
//  }
//}
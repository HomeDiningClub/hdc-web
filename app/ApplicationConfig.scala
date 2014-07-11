//import java.util.UUID
//import models.base.AbstractEntity
//import org.springframework.context.annotation.{Bean, Configuration}
//import org.springframework.context.ApplicationListener
//import org.springframework.data.neo4j.config.{Neo4jConfiguration, EnableNeo4jRepositories}
//import org.springframework.data.neo4j.lifecycle.{AfterDeleteEvent, AfterSaveEvent, BeforeSaveEvent}
//import play.api.Logger
//
//@Configuration
//@EnableNeo4jRepositories
//class ApplicationConfig extends Neo4jConfiguration {
//
//  @Bean
//  def beforeSaveEventApplicationListener(): ApplicationListener[BeforeSaveEvent[AbstractEntity]] = {
//    new ApplicationListener[BeforeSaveEvent[AbstractEntity]]() {
//
//      override def onApplicationEvent(event: BeforeSaveEvent[AbstractEntity]) {
//        val entity = event.getEntity
//        entity.setUniqueId(UUID.randomUUID)
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
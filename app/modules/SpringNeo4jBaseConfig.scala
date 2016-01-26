package modules

import javax.inject._
import play.api.inject.ApplicationLifecycle
import com.typesafe.config.ConfigException
import com.typesafe.config.ConfigFactory
import org.springframework.beans.BeansException
import org.springframework.beans.factory.BeanCreationException
import org.springframework.beans.factory.ObjectFactory
import org.springframework.context.annotation.Bean
import org.springframework.data.auditing.IsNewAwareAuditingHandler
import org.springframework.data.neo4j.config.Neo4jConfiguration
import org.springframework.data.neo4j.lifecycle.AuditingEventListener

class SpringNeo4jBaseConfig extends Neo4jConfiguration {

  //@Inject var lifecycle: ApplicationLifecycle = null

  // TODO: Enable Database event-triggers
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

}
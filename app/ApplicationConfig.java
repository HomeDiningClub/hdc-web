import models.base.AbstractEntity;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.neo4j.config.EnableNeo4jRepositories;
import org.springframework.data.neo4j.config.Neo4jConfiguration;
import org.springframework.data.neo4j.lifecycle.BeforeSaveEvent;

//@Configuration
//@EnableNeo4jRepositories
//public class ApplicationConfig extends Neo4jConfiguration {
//    @Bean
//    ApplicationListener<BeforeSaveEvent> beforeSaveEventApplicationListener() {
//        return new ApplicationListener<BeforeSaveEvent>() {
//            @Override
//            public void onApplicationEvent(BeforeSaveEvent event) {
//                AbstractEntity entity = (AbstractEntity) event.getEntity();
//                Long id = entity.graphId;
//                //entity.setUniqueId(acmeIdFactory.create());
//            }
//        };
//    }

//    @Bean
//    ApplicationListener<AfterSaveEvent> afterSaveEventApplicationListener() {
//        return new ApplicationListener<AfterSaveEvent>() {
//            @Override
//            public void onApplicationEvent(AfterSaveEvent event) {
//                AcmeEntity entity = (AcmeEntity) event.getEntity();
//                auditLog.onEventSaved(entity);
//            }
//        };
//    }
//
//    @Bean
//    ApplicationListener<DeleteEvent> deleteEventApplicationListener() {
//        return new ApplicationListener<DeleteEvent>() {
//            @Override
//            public void onApplicationEvent(DeleteEvent event) {
//                AcmeEntity entity = (AcmeEntity) event.getEntity();
//                auditLog.onEventDeleted(entity);
//            }
//        };
//    }
//}
//
//class Neo4JEventListeners {
//
//    @Bean
//    def beforeSaveEventApplicationListener(): ApplicationListener[BeforeSaveEvent[Node]] = {
//      return new ApplicationListener[BeforeSaveEvent[Node]]() {
//
//        @Override
//        override def onApplicationEvent(event: BeforeSaveEvent[Node]) {
//          val entity = event.getEntity().asInstanceOf[AbstractEntity]
//          val graphid = entity
//        }
//      }
//    }
//
//}

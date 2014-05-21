//package beans
//
//import play.api.{GlobalSettings, Configuration, Plugin}
//import play.api.Mode.Mode
//import play.core.SourceMapper
//import java.io.File
//import play.api.Play.{current => CurrentApplication}
//
// TODO remove
//
//class PlayApplicationImpl extends play.api.Application {
//  override def path: File = CurrentApplication.path
//  override def sources: Option[SourceMapper] = CurrentApplication.sources
//  override def global: GlobalSettings = CurrentApplication.global
//  override def classloader: ClassLoader = CurrentApplication.classloader
//  override def mode: Mode = CurrentApplication.mode
//  override def configuration: Configuration = CurrentApplication.configuration
//  override def plugins: Seq[Plugin] = CurrentApplication.plugins
//}

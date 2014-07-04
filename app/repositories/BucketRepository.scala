package repositories

import play.Play
import fly.play.s3.S3
import org.springframework.stereotype.{Component, Repository}

@Repository
class BucketRepository {

  private lazy val bucketStore: String = Play.application.configuration.getString("aws.s3bucket")
  lazy val S3Bucket = S3(bucketStore)

}

package models.formdata

case class BlogPostsForm(
id: Option[String],
title:Option[String],
maintext: Option[String],
mainImage: Option[String]
){}

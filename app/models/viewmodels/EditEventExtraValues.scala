package models.viewmodels

case class EditEventExtraValues(mainImagePrev : Option[List[String]],
                                  imagesPrev : Option[List[String]],
                                  mainImageMaxNr : Integer,
                                  imagesMaxNr : Integer,
                                  nrOfTotalGuestsBookedForThisEvent: Integer
                                  )

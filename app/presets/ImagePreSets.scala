package presets

import models.files.FileTransformation
import constants.FileTransformationConstants

object ImagePreSets {

  val userCredentialImages: List[FileTransformation] = List[FileTransformation](
    new FileTransformation("mini", 30,30, FileTransformationConstants.COVER),
    new FileTransformation("thumbnail", 100,100, FileTransformationConstants.COVER)
  )

  val recipeImages: List[FileTransformation] = List[FileTransformation](
    new FileTransformation("thumbnail", 150,100, FileTransformationConstants.FIT),
    new FileTransformation("box", 275,160, FileTransformationConstants.FIT),
    new FileTransformation("normal", 460,305, FileTransformationConstants.FIT),
    new FileTransformation("big", 800,600, FileTransformationConstants.FIT)
  )

  val profileImages: List[FileTransformation] = List[FileTransformation](
    new FileTransformation("thumbnail", 150,100, FileTransformationConstants.FIT),
    new FileTransformation("box", 275,160, FileTransformationConstants.FIT),
    new FileTransformation("normal", 460,305, FileTransformationConstants.FIT),
    new FileTransformation("big", 800,600, FileTransformationConstants.FIT)
  )

  val testImages: List[FileTransformation] = List[FileTransformation](
    new FileTransformation("thumbnail", 150,100, FileTransformationConstants.FIT),
    new FileTransformation("box", 275,160, FileTransformationConstants.FIT),
    new FileTransformation("normal", 460,305, FileTransformationConstants.FIT),
    new FileTransformation("big", 800,600, FileTransformationConstants.FIT)
  )

}

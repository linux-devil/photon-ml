package com.linkedin.photon.ml.supervised

import com.linkedin.photon.ml.data.LabeledPoint
import com.linkedin.photon.ml.supervised.classification.BinaryClassifier
import com.linkedin.photon.ml.supervised.model.GeneralizedLinearModel
import com.linkedin.photon.ml.supervised.regression.Regression
import org.apache.spark.rdd.RDD
/**
 * Created by asaha on 10/6/15.
 */
class PredictionNonNegativeValidator extends ModelValidator[GeneralizedLinearModel] {

  override def validateModelPredictions(model:GeneralizedLinearModel, data:RDD[LabeledPoint]) : Unit = {
    val features = data.map { x => x.features }
    var predictions:RDD[Double] = null
    model match {
      case r:Regression =>
        predictions = r.predictAll(features)

      case b:BinaryClassifier =>
        predictions = b.predictClassAllWithThreshold(features, 0.5)

      case _ =>
        throw new IllegalArgumentException("Don't know how to handle models of type [" + model.getClass.getName + "]")
    }

    val invalidCount = predictions.filter(x => x<0).count
    if (invalidCount > 0) {
      throw new IllegalStateException("Found [" + invalidCount + "] samples with invalid negative predictions")
    }
  }
}
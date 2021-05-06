
package org.micromanager.pseudochannels;

import ij.process.ImageProcessor;


import org.micromanager.data.*;

import org.micromanager.PropertyMap;
import org.micromanager.PropertyMaps;
import org.micromanager.Studio;


public class PseudoChannelProcessor extends Processor {

   // Valid rotation values.
   public static final int C1 = 1;
   public static final int C2 = 2;
   public static final int C3 = 3;
   public static final int C4 = 4;

   private final Studio studio_;
   int channels_;
   String slices_;
   boolean useSlices_;


   public PseudoChannelProcessor(Studio studio, int channels, String slices,
         boolean useSlices) {
      studio_ = studio;
      channels_ = channels;
      slices_ = slices;
      useSlices_ = useSlices;
   }

   /**
    * Process one image.
    */
   @Override
   public void processImage(Image image, ProcessorContext context) {
      // to allow processing old data, we do not check for the camera when no 
      // camera was selected
      context.outputImage(
              transformImage(studio_, image, useSlices_, slices_, channels_));
   }

   /**
    * Executes image transformation
    * First mirror the image if requested, than rotate as requested
    * 
    * @param studio
    * @param image Image to be transformed.
    * @param useSlices Whether or not to do a ZStack
    * @param slices Number of Slices for the ZStack
    * @param channels Number of channels
    * @return - Transformed Image, otherwise a copy of the input
    */
   public static Image transformImage(Studio studio, Image image,
         boolean useSlices, String slices, int channels) {
      
      ImageProcessor proc = studio.data().ij().createProcessor(image);

      // Insert some metadata to indicate what we did to the image.
      PropertyMap.Builder builder;
      PropertyMap userData = image.getMetadata().getUserData();
      if (userData != null) {
         builder = userData.copyBuilder();
      }
      else {
         builder = PropertyMaps.builder();
      }
      builder.putString("PseudoChannel-Channels", String.valueOf(channels));
      builder.putString("PseudoChannel-useSlices", useSlices ? "On" : "Off");
      builder.putString("PseudoChannel-Slices", slices);
      Metadata newMetadata = image.getMetadata().copyBuilderPreservingUUID().userData(builder.build()).build();

      //Do the actual processing of the image
      Coords.Builder coordsBuilder = image.getCoords().copyBuilder();
      Coords old_coords = image.getCoords();
      coordsBuilder.c(old_coords.getT()%channels);
      int time = (int) java.lang.Math.floor(old_coords.getT()/channels);
      coordsBuilder.t(time);
      System.out.println(channels);
      Image result = studio.data().ij().createImage(proc, coordsBuilder.build(),
            newMetadata);
      return result;
   }
}

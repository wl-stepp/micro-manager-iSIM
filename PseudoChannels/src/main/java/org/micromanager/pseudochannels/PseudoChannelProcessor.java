
package org.micromanager.pseudochannels;

import ij.process.ImageProcessor;


import org.micromanager.data.*;

import org.micromanager.PropertyMap;
import org.micromanager.PropertyMaps;
import org.micromanager.Studio;

import java.io.IOException;


public class PseudoChannelProcessor implements Processor {

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
    * @param studio main Studio instance
    * @param image Image to be transformed.
    * @param useSlices Whether or not to do a ZStack
    * @param slices Number of Slices for the ZStack
    * @param channels Number of channels
    * @return - Transformed Image, otherwise a copy of the input
    */
   public static Image transformImage(Studio studio, Image image,
         boolean useSlices, String slices, int channels) {

      int slices_int;
      if (useSlices) {
         slices_int = Integer.valueOf(slices);
      } else {
         slices_int = 1;
      }
//      ImageProcessor proc = studio.data().ij().createProcessor(image);

      //TODO: these changes should also be reflected in the metadata of the image.
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

      // Do the actual processing of the image
      Coords.Builder coordsBuilder = image.getCoords().copyBuilder();
      Coords old_coords = image.getCoords();

      int channel = (int) java.lang.Math.floor((old_coords.getT() % (channels * slices_int)) /slices_int) ;
      coordsBuilder.c(channel);

      int time = (int) java.lang.Math.floor(old_coords.getT()/channels/slices_int);

      int zPos = old_coords.getT()%slices_int;
      // switch the direction of z for every second frame
      if (useSlices & channel%2 == 1){
         zPos = slices_int - 1 - zPos;
      }

      if (useSlices){
         coordsBuilder.z(zPos);
      } else {
         zPos = 0;
      }

      coordsBuilder.t(time);


      System.out.printf("slices %d, channels %d%n", slices_int, channels);
      System.out.printf("time %d, zPos %d, channel %d%n",time, zPos, channel);
      return image.copyWith(coordsBuilder.build(), newMetadata);
   }
}

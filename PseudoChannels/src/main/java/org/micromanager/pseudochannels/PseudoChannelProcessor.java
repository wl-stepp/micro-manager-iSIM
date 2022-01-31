
package org.micromanager.pseudochannels;

import ij.process.ImageProcessor;


import org.micromanager.data.*;

import org.micromanager.PropertyMap;
import org.micromanager.PropertyMaps;
import org.micromanager.Studio;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class PseudoChannelProcessor implements Processor {

   private final Studio studio_;
   int channels_;
   String slices_;
   boolean useSlices_;
   double[] slicePositions_;


   public PseudoChannelProcessor(Studio studio, int channels, String slices,
                                 boolean useSlices, double[] slicePositions) {
      studio_ = studio;
      channels_ = channels;
      slices_ = slices;
      useSlices_ = useSlices;
      slicePositions_ =  slicePositions;
   }

   /**
    * Process one image.
    */
   @Override
   public void processImage(Image image, ProcessorContext context) {
      // to allow processing old data, we do not check for the camera when no 
      // camera was selected
      context.outputImage(
              transformImage(studio_, image, useSlices_, slices_, channels_, slicePositions_));
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
         boolean useSlices, String slices, int channels, double[] slicePositions) {

      int slices_int;
      if (useSlices) {
         slices_int = Integer.parseInt(slices);
      } else {
         slices_int = 1;
      }
//      ImageProcessor proc = studio.data().ij().createProcessor(image);

      //TODO: Make this also possible in different order for channels/slices

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
         if (slicePositions != null) {
            newMetadata = newMetadata.copyBuilderPreservingUUID().zPositionUm(slicePositions[zPos]).build();
         }
      } else {
         zPos = 0;
      }

      coordsBuilder.t(time);

      System.out.printf("slices %d, channels %d%n", slices_int, channels);
      System.out.printf("time %d, zPos %d, channel %d%n",time, zPos, channel);
      System.out.println("Slices: " + Arrays.toString(slicePositions));
      return image.copyWith(coordsBuilder.build(), newMetadata);
   }

//   public static void setSlicePositions(float[] newSlicePositions){
//      slicePositions_ = newSlicePositions;
//   }
}

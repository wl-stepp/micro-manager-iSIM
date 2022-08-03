
package org.micromanager.imageinjector;

import org.micromanager.data.*;
import org.micromanager.Studio;


import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;


public class TimeInjectorProcessor implements Processor {

   private final Studio studio_;

   public TimeInjectorProcessor(Studio studio) {
      studio_ = studio;
   }

   private static double translateTime(String time) {
      time = time.split(" \\+")[0];
      System.out.println(time);
      LocalDateTime localDateTime = LocalDateTime.parse(time,
              DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS") );
      return (double) localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
   }

   /**
    * Process one image.
    */
   @Override
   public void processImage(Image image, ProcessorContext context) {
      double start_time = translateTime(context.getSummaryMetadata().getStartDate());
      context.outputImage(
              transformImage(studio_, image, start_time));
   }

   /**
    * Executes image transformation
    *
    * @param studio main Studio instance
    * @param image Image to be transformed.
    * @return - Transformed Image, otherwise a copy of the input
    */
   public static Image transformImage(Studio studio, Image image, double start_millis) {
      // Even if the injector is turned on just return the original image if the time is already there
      if (image.getMetadata().hasElapsedTimeMs()){
         return image;
      }

      // Insert the received time into the metadata
      Metadata meta = null;
      try {
         meta = studio.acquisitions().generateMetadata(image, false);
      } catch (Exception e) {
         System.out.print("ERROR in generating Metadata");
         e.printStackTrace();
      }

      // Translate the received time to milliseconds
      assert meta != null;
      double now_millis = translateTime(meta.getReceivedTime());

      // Calculate and insert the elapsed time
      double new_time = now_millis - start_millis;
      Metadata.Builder builder = meta.copyBuilderPreservingUUID();
      builder.elapsedTimeMs(new_time);
      Metadata new_meta = builder.build();

      return image.copyWithMetadata(new_meta);
   }
}

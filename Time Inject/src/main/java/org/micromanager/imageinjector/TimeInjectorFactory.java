package org.micromanager.imageinjector;

import org.micromanager.data.Processor;
import org.micromanager.data.ProcessorFactory;

import org.micromanager.Studio;

public class TimeInjectorFactory implements ProcessorFactory {
   private final Studio studio_;

   public TimeInjectorFactory(Studio studio) {
      studio_ = studio;
   }

   @Override
   public Processor createProcessor() {
      return new TimeInjectorProcessor(studio_);
   }
}

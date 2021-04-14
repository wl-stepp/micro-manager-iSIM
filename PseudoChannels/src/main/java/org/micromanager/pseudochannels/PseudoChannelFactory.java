///////////////////////////////////////////////////////////////////////////////
//PROJECT:       Micro-Manager
//SUBSYSTEM:     mmstudio
//-----------------------------------------------------------------------------
//
// AUTHOR:       Chris Weisiger
//
// COPYRIGHT:    University of California, San Francisco, 2011, 2015
//
// LICENSE:      This file is distributed under the BSD license.
//               License text is included with the source distribution.
//
//               This file is distributed in the hope that it will be useful,
//               but WITHOUT ANY WARRANTY; without even the implied warranty
//               of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
//               IN NO EVENT SHALL THE COPYRIGHT OWNER OR
//               CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
//               INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES.

package org.micromanager.pseudochannels;

import org.micromanager.data.Processor;
import org.micromanager.data.ProcessorFactory;

import org.micromanager.PropertyMap;
import org.micromanager.Studio;

public class PseudoChannelFactory implements ProcessorFactory {
   private final PropertyMap settings_;
   private final Studio studio_;
   private PseudoChannelProcessor processor_;

   public PseudoChannelFactory(PropertyMap settings, Studio studio) {
      settings_ = settings;
      studio_ = studio;
   }

   public void changeProcessor(int channels){
      processor_.channels_ = channels;
   }

   @Override
   public Processor createProcessor() {
      processor_ = new PseudoChannelProcessor(studio_,
              settings_.getInteger("channels", 1),
              settings_.getString("slices", "10"),
              settings_.getBoolean("useSlices", false));
      return processor_;
   }
}

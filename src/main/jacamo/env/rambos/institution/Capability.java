/*******************************************************************************
 * MIT License
 *
 * Copyright (c) Igor Conrado Alves de Lima <igorcadelima@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *******************************************************************************/
package rambos.institution;

import cartago.OpFeedbackParam;
import jason.JasonException;
import jason.asSemantics.Agent;
import jason.asSyntax.parser.ParseException;

/**
 * An enum that contains the basic capabilities provided by the sanctioning mechanism.
 * 
 * @author igorcadelima
 *
 */
enum Capability {
  DETECTOR {
    @Override
    void getPlansForAgentType(String agentType, OpFeedbackParam<Object> plans) {
      AgentType type = AgentType.valueOf(agentType.toUpperCase());
      type.getPlansForDetector(plans);

    }
  };

  abstract void getPlansForAgentType(String type, OpFeedbackParam<Object> plans);

  /**
   * An enum that contains the types of agents which are supported.
   */
  enum AgentType {
    JASON {
      @Override
      void getPlansForDetector(OpFeedbackParam<Object> plans) {
        try {
          Agent agent = new Agent();
          agent.initAg();
          agent.parseAS(Capability.class.getResourceAsStream("/agt/detector.asl"));
          plans.set(agent.getPL()
                         .getPlans()
                         .toArray());
        } catch (ParseException | JasonException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
    };

    /**
     * Get Jason plans for detectors.
     * 
     * @param plans output parameter used to return the plans
     */
    abstract void getPlansForDetector(OpFeedbackParam<Object> plans);
  }
}

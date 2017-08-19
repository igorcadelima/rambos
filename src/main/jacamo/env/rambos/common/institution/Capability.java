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
package rambos.common.institution;

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
    void getPlansForAgentType(AgentType type, OpFeedbackParam<Object> plans) {
      type.getPlansForDetector(plans);
    }
  },

  EVALUATOR {
    @Override
    void getPlansForAgentType(AgentType type, OpFeedbackParam<Object> plans) {
      type.getPlansForEvaluator(plans);
    }
  };

  abstract void getPlansForAgentType(AgentType type, OpFeedbackParam<Object> plans);

  /**
   * An enum that contains the types of agents which are supported.
   */
  enum AgentType {
    JASON {
      Object[] getPlansOf(String file) {
        Agent agent = new Agent();
        try {
          agent.initAg();
          agent.parseAS(AgentType.class.getResourceAsStream(file));
        } catch (ParseException | JasonException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
        return agent.getPL()
                    .getPlans()
                    .toArray();
      }

      @Override
      void getPlansForDetector(OpFeedbackParam<Object> plans) {
        plans.set(getPlansOf("/agt/detector.asl"));
      }

      @Override
      void getPlansForEvaluator(OpFeedbackParam<Object> plans) {
        plans.set(getPlansOf("/agt/evaluator.asl"));
      }
    };

    /**
     * Get Jason plans for detectors.
     * 
     * @param plans output parameter used to return the plans
     */
    abstract void getPlansForDetector(OpFeedbackParam<Object> plans);

    /**
     * Get Jason plans for evaluator.
     * 
     * @param plans output parameter used to return the plans
     */
    abstract void getPlansForEvaluator(OpFeedbackParam<Object> plans);
  }
}

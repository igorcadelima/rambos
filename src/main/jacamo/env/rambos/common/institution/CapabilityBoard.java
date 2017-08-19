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

import cartago.Artifact;
import cartago.OPERATION;
import cartago.OpFeedbackParam;
import rambos.common.institution.Capability.AgentType;

/**
 * Board where capabilities may be stored so that agents may acquire them through operations.
 * 
 * @author igorcadelima
 *
 */
public final class CapabilityBoard extends Artifact {
  /**
   * Return default plans to perform actions which are supposed to be conducted by holders of a
   * certain {@code capability} depending on the {@code agentType}.
   * 
   * @param capability capability to be acquired
   * @param agentType type of the agent which should acquire the capability
   * @param plans output parameter used to return the plans
   */
  @OPERATION
  public void acquire(String capability, String agentType, OpFeedbackParam<Object> plans) {
    Capability capab = Capability.valueOf(capability.toUpperCase());
    AgentType type = AgentType.valueOf(agentType.toUpperCase());
    capab.getPlansForAgentType(type, plans);
  }
}

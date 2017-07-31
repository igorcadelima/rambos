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
package rambos;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jason.asSyntax.ASSyntax;
import jason.asSyntax.Atom;
import jason.asSyntax.Literal;
import jason.asSyntax.LogicalFormula;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.NumberTermImpl;
import jason.asSyntax.Term;
import jason.asSyntax.VarTerm;
import jason.asSyntax.parser.ParseException;
import npl.TimeTerm;
import rambos.Contents.Functor;

/**
 * @author igorcadelima
 *
 */
class ContentStringParser implements ContentParser<String> {
  @Override
  public IContent parse(String in) {
    Literal literal;
    try {
      literal = parseObligationProposition(in);
    } catch (ParseException e) {
      literal = parseFailProposition(in);
    }
    return Contents.of(literal);
  }

  /**
   * Parse string to obligation literal.
   * 
   * @param obl string with obligation
   * @return obligation literal if {@code content} is a well-formed string
   * @throws ParseException
   */
  private Literal parseObligationProposition(String obl) throws ParseException {
    String obligationRegex =
        "obligation\\s*\\(\\s*(\\w+)\\s*,\\s*(\\w+)\\s*,\\s*(.+?)\\s*,\\s*(.+?)\\s*\\)";
    Pattern pattern = Pattern.compile(obligationRegex);
    Matcher matcher = pattern.matcher(obl);

    if (matcher.matches()) {
      String agent = matcher.group(1);
      String maintCondition = matcher.group(2);
      String goal = matcher.group(3);
      String deadline = matcher.group(4);
      Literal literal = ASSyntax.createLiteral(Functor.OBLIGATION.lowercase());

      // Parse and add agent term
      literal.addTerm(parseAgent(agent));

      // Parse and add condition term
      literal.addTerm(parseMaintenanceCondition(maintCondition));

      // Parse and add goal term
      literal.addTerm(ASSyntax.parseFormula(goal));

      // Solve and add deadline term
      literal.addTerm(solveTimeExpression(deadline));
      return literal;
    }
    throw new ParseException();
  }

  /**
   * Parse maintenance condition using {@link ASSyntax#parseFormula(String)} and return the result
   * as a {@link Term}. If {@code maintCondition} is equal to the id of the element, then
   * {@code condition} is returned. Otherwise, the maintenance condition is returned.
   * 
   * @param maintCondition maintenance condition of the element
   * @return parsed condition as a {@link Term}
   * @throws ParseException
   */
  private Term parseMaintenanceCondition(String maintCondition) throws ParseException {
    LogicalFormula conditionLiteral = ASSyntax.parseFormula(maintCondition);
    return conditionLiteral;
  }

  /**
   * Parse agent name to {@link VarTerm} or {@link Atom} depending on what it really represents.
   * 
   * @param agent name of the agent
   * @return term with given agent name.
   */
  private Term parseAgent(String agent) {
    char agentInitial = agent.charAt(0);
    if (Character.isUpperCase(agentInitial)) {
      return new VarTerm(agent);
    } else {
      return new Atom(agent);
    }
  }

  /**
   * Solve a time expression such as {@code `now` + `2 days`} or {@code now + 2 days} and return the
   * result as {@link NumberTerm} in milliseconds.
   * 
   * @param time string with expression to be solved
   * @return time in milliseconds
   */
  private NumberTerm solveTimeExpression(String time) {
    String[] deadlineTerms = time.replace("`", "")
                                 .split("((?<=\\+)|(?=\\+)|(?<=\\-)|(?=\\-))");
    double x = parseTimeTerm(deadlineTerms[0]).solve();

    for (int i = 1; i < deadlineTerms.length; i += 2) {
      double y = parseTimeTerm(deadlineTerms[i + 1]).solve();
      x = solveNumericalExpression(x, deadlineTerms[i], y);
    }
    return new NumberTermImpl(x);
  }

  /**
   * Solve numerical expression given two double terms and an operator as string.
   * 
   * @param x first term
   * @param operator either {@code "+"} or {@code "-"}
   * @param y second term
   * @return solved expression
   */
  private double solveNumericalExpression(double x, String operator, double y) {
    switch (operator) {
      case "+":
        return x + y;
      case "-":
        return x - y;
      default:
        throw new IllegalArgumentException("Unknown operator");
    }
  }

  /**
   * Parse proposition to {@link Literal} whose functor is {@literal "fail"}.
   * 
   * @param proposition deontic proposition
   * @return fail literal content if {@code proposition} is a well-formed string or {@code null},
   *         otherwise
   */
  private Literal parseFailProposition(String proposition) {
    String failureRegex = "fail\\s*\\(\\s*(.+)\\s*\\)";
    Pattern pattern = Pattern.compile(failureRegex);
    Matcher matcher = pattern.matcher(proposition);

    if (matcher.matches()) {
      String reasonStr = matcher.group(1);
      Atom reason = new Atom(reasonStr);
      return ASSyntax.createLiteral(Functor.FAIL.lowercase(), reason);
    }
    return null;
  }

  /**
   * Parse argument to {@link TimeTerm}.
   * 
   * @param time
   * @return {@code time} as an instance of {@link TimeTerm}
   */
  private TimeTerm parseTimeTerm(String time) {
    TimeTerm term = null;
    String[] timeTerms = time.trim()
                             .split(" ");

    if (timeTerms.length == 1) {
      // These conditions are necessary due to a bug in TimeTerm
      term = timeTerms[0].equals("now") ? new TimeTerm(0, null) : new TimeTerm(0, timeTerms[0]);
    } else {
      term = new TimeTerm(Long.parseLong(timeTerms[0]), timeTerms[1]);
    }
    return term;
  }
}

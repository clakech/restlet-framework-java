/*
 * Copyright 2005-2008 Noelios Consulting.
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the "License"). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL HEADER in each file and
 * include the License file at http://www.opensource.org/licenses/cddl1.txt If
 * applicable, add the following below this CDDL HEADER, with the fields
 * enclosed by brackets "[]" replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */

package org.restlet.ext.jaxrs.util;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.Path;

import org.restlet.ext.jaxrs.exceptions.IllegalPathException;
import org.restlet.ext.jaxrs.exceptions.IllegalPathOnClassException;
import org.restlet.ext.jaxrs.exceptions.IllegalPathOnMethodException;
import org.restlet.ext.jaxrs.exceptions.MissingAnnotationException;
import org.restlet.ext.jaxrs.wrappers.AbstractJaxRsWrapper;
import org.restlet.ext.jaxrs.wrappers.AbstractMethodWrapper;
import org.restlet.ext.jaxrs.wrappers.ResourceClass;
import org.restlet.util.Resolver;
import org.restlet.util.Template;
import org.restlet.util.Variable;

/**
 * Immutable
 * 
 * @author Stephan Koops
 * 
 */
public class PathRegExp {

    /**
     * VariableResolver that returns "" for every variable name
     * 
     * @author Stephan Koops
     */
    private static class EverNullVariableResolver implements Resolver {
        public String resolve(String variableName) {
            return "";
        }
    }

    /**
     * The PathRegExp with an empty path.
     */
    public static PathRegExp EMPTY = new PathRegExp("", true);

    private static EverNullVariableResolver EmptyStringVariableResolver = new EverNullVariableResolver();

    private static final String VARNAME_FUER_REST = "restlet.jaxrs.rest";

    /**
     * Creates a {@link PathRegExp} for a root resource class.
     * 
     * @param jaxRsRootResourceClass
     * @return
     * @throws MissingAnnotationException
     * @throws IllegalArgumentException
     * @throws IllegalPathOnClassException
     * @see {@link #EMPTY}
     */
    public static PathRegExp createForClass(Class<?> jaxRsRootResourceClass)
            throws MissingAnnotationException, IllegalArgumentException,
            IllegalPathOnClassException {
        try {
            return new PathRegExp(ResourceClass
                    .getPathAnnotation(jaxRsRootResourceClass));
        } catch (IllegalPathException e) {
            throw new IllegalPathOnClassException(e);
        }
    }

    /**
     * Creates a {@link PathRegExp} for a sub resource method or sub resource
     * locator. Returns {@link #EMPTY}, if the method is not annotated with
     * &#64;Path.
     * 
     * @param javaMethod
     * @return the {@link PathRegExp}. Never returns null.
     * @throws IllegalPathOnMethodException
     * @throws MissingAnnotationException
     * @throws IllegalArgumentException
     *                 if the javaMethod was null
     * @see {@link #EMPTY}
     */
    public static PathRegExp createForMethod(Method javaMethod)
            throws IllegalPathOnMethodException, IllegalArgumentException {
        Path pathAnnotation = AbstractMethodWrapper
                .getPathAnnotationOrNull(javaMethod);
        if (pathAnnotation == null)
            return EMPTY;
        try {
            return new PathRegExp(pathAnnotation);
        } catch (IllegalPathException e) {
            throw new IllegalPathOnMethodException(e);
        }
    }

    /**
     * get the pattern of the path. Ensures also, that it starts with a "/"
     * 
     * @param path
     * @return
     * @throws IllegalPathException
     */
    private static String getPathPattern(Path path)
            throws IllegalArgumentException, IllegalPathException {
        if (path == null)
            throw new IllegalArgumentException("The path must not be null");
        String pathPattern = AbstractJaxRsWrapper.getPathTemplate(path);
        return Util.ensureStartSlash(pathPattern);
    }

    private boolean isEmptyOrSlash;

    /** Contains the number of literal chars in this Regular Expression */
    private Integer noLitChars;

    private String pathPattern;

    private Template template;

    private PathRegExp(Path path) throws IllegalArgumentException,
            IllegalPathException {
        this(getPathPattern(path), path.limited());
    }

    /**
     * Is intended for internal use and testing. Otherwise use the methods
     * create*
     * 
     * @param pathPattern
     * @param limitedToOneSegment
     * @see #createForClass(Class)
     * @see #createForMethod(Method)
     * @see #EMPTY
     */
    @Deprecated
    public PathRegExp(String pathPattern, boolean limitedToOneSegment) {
        this.pathPattern = pathPattern;
        this.isEmptyOrSlash = Util.isEmptyOrSlash(pathPattern);
        StringBuilder patternStb = new StringBuilder(pathPattern);
        if (!pathPattern.endsWith("/"))
            patternStb.append('/');
        patternStb.append('{');
        patternStb.append(VARNAME_FUER_REST);
        patternStb.append('}');
        this.template = new Template(patternStb.toString(),
                org.restlet.util.Template.MODE_EQUALS);
        Variable defaultVariable = this.template.getDefaultVariable();
        if (limitedToOneSegment)
            defaultVariable.setType(Variable.TYPE_URI_SEGMENT);
        else
            defaultVariable.setType(Variable.TYPE_URI_PATH);

        Variable restVar = template.getVariables().get(VARNAME_FUER_REST);
        if (restVar == null) {
            restVar = new Variable(Variable.TYPE_ALL);
            template.getVariables().put(VARNAME_FUER_REST, restVar);
        }
        restVar.setRequired(false);
    }

    @Override
    public boolean equals(Object anotherObject) {
        if (this == anotherObject)
            return true;
        if (!(anotherObject instanceof PathRegExp))
            return false;
        PathRegExp otherRegExp = (PathRegExp) anotherObject;
        return this.getWithEmptyVars().equals(otherRegExp.getWithEmptyVars());
    }

    /**
     * @return Returns the number of capturing groups.
     */
    public int getNumberOfCapturingGroups() {
        return this.template.getVariableNames().size();
    }

    /**
     * See Footnode to JSR-311-Spec, Section 2.5, Algorithm, Part 1e
     * 
     * @return Returns the number of literal chars in the path patern
     */
    public int getNumberOfLiteralChars() {
        if (noLitChars == null) {
            noLitChars = getWithEmptyVars().length();
        }
        return noLitChars;
    }

    /**
     * @return Returns the path pattern.
     */
    public String getPathPattern() {
        return pathPattern;
    }

    /**
     * @return
     */
    private String getWithEmptyVars() {
        return this.template.format(EmptyStringVariableResolver);
    }

    @Override
    public int hashCode() {
        return this.template.hashCode();
    }

    /**
     * Checks if the URI template is empty or only a slash.
     * 
     * @return
     */
    public boolean isEmptyOrSlash() {
        return isEmptyOrSlash;
    }

    /**
     * Checks if this regular expression matches the given remaining path.
     * 
     * @param remainingPath
     * @return Returns an MatchingResult, if the remainingPath matches to this
     *         template, or null, if not.
     */
    @SuppressWarnings("unchecked")
    public MatchingResult match(RemainingPath remainingPath) {
        String givenPath = remainingPath.getWithoutParams();
        Map<String, String> templateVars = new HashMap<String, String>();
        boolean pathSuppl = !givenPath.endsWith("/");
        if (pathSuppl)
            givenPath += '/';
        boolean matches = template.parse(givenPath, (Map) templateVars) >= 0;
        if (!matches)
            return null;
        String finalMatchingGroup = templateVars.remove(VARNAME_FUER_REST);
        if (finalMatchingGroup.length() > 0) {
            if (pathSuppl && finalMatchingGroup.endsWith("/"))
                finalMatchingGroup = finalMatchingGroup.substring(0,
                        finalMatchingGroup.length() - 1);
            if (!finalMatchingGroup.startsWith("/"))
                finalMatchingGroup = "/" + finalMatchingGroup;
        }
        String finalCapturingGroup = templateVars.get(Util
                .getLastElement(template.getVariableNames()));
        // TODO JSR311: finalCapturingGroup habe ich noch nicht richtig
        // verstanden.
        if (finalCapturingGroup == null)
            finalCapturingGroup = ""; // TODO ob das stimmt, weiss ich nicht
        finalCapturingGroup = finalMatchingGroup;
        return new MatchingResult(templateVars, finalMatchingGroup,
                finalCapturingGroup, templateVars.size());
    }

    /**
     * Checks if this regular expression matches the given remaining path.
     * 
     * @param remainingPath
     * @return Returns an MatchingResult, if the remainingPath matches to this
     *         template, or null, if not.
     */
    public MatchingResult match(String remainingPath) {
        return this.match(new RemainingPath(remainingPath));
    }

    /**
     * See JSR-311-Spec, Section 2.5, Algorithm, part 3a, point 1.
     * 
     * @param remainingPath
     * @return
     */
    public boolean matchesWithEmpty(RemainingPath remainingPath) {
        MatchingResult matchingResult = this.match(remainingPath);
        if (matchingResult == null)
            return false;
        return matchingResult.getFinalCapturingGroup().isEmptyOrSlash();
    }

    @Override
    public String toString() {
        return this.pathPattern;
    }
}
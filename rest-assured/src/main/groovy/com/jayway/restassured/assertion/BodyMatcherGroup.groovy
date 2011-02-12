/*
 * Copyright 2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jayway.restassured.assertion

class BodyMatcherGroup {
  private List bodyAssertions = []
  def leftShift(Object bodyMatcher) {
    bodyAssertions << bodyMatcher
  }

  def isFulfilled(response, content) {
    def treatedContent
    if(content instanceof InputStreamReader) {
      treatedContent = content.readLines().join()
    } else {
      treatedContent = content
    }

    bodyAssertions.each { assertion ->
      assertion.isFulfilled(response, treatedContent)
    }
  }

  public boolean containsMatchers() {
    !bodyAssertions.isEmpty()
  }

  def boolean requiresTextParsing() {
    def numberOfRequires = 0
    def numberOfNonRequires = 0
    bodyAssertions.each { matcher ->
      if(matcher.requiresTextParsing()) {
        numberOfRequires++
      } else {
        numberOfNonRequires++
      }
    }
    throwExceptionIfIllegalBodyAssertionCombinations(numberOfRequires, numberOfNonRequires)

    return numberOfRequires != 0
  }

  def String getDescriptions() {
    String descriptions = ""
    bodyAssertions.each {
      descriptions = it.getDescription()
    }
    return descriptions
  }

  private def throwExceptionIfIllegalBodyAssertionCombinations(int numberOfRequires, int numberOfNonRequires) {
    if (numberOfRequires > 0 && numberOfNonRequires > 0) {
      String matcherDescription = "";
      bodyAssertions.each { matcher ->
        def String hamcrestDescription = matcher.getDescription()
        matcherDescription += "\n$hamcrestDescription "
        if (matcher.requiresTextParsing()) {
          matcherDescription += "which requires 'TEXT'"
        } else {
          matcherDescription += "which cannot be 'TEXT'"
        }
      }
      throw new IllegalStateException("""Currently you cannot mix body expectations that require different content types for matching.
For example XPath and full body matching requires TEXT content and JSON/XML matching requires JSON/XML/ANY mapping. You need to split conflicting matchers into two tests. Your matchers are:$matcherDescription""")
    }
  }
}

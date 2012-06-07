// Copyright (c) 2011-12 Paul Butcher
// 
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
// 
// The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.
// 
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
// THE SOFTWARE.

package org.scalamock.scalatest

import org.scalamock.MockFactoryBase
import org.scalatest.{AbstractSuite, Reporter, Stopper, Suite, Tracker}
import org.scalatest.exceptions.TestFailedException

/** Trait that can be mixed into a [[http://www.scalatest.org/ ScalaTest]] suite to provide
  * mocking support.
  *
  * See [[org.scalamock]] for overview documentation.
  */
trait MockFactory extends AbstractSuite with MockFactoryBase { this: Suite =>
  
  type ExpectationException = RuntimeException
  
  override def withFixture(test: NoArgTest) {

    if (autoVerify)
      withExpectations { test() }
    else
      test()
  }
  
  protected def newExpectationException(message: String, stackDepth: Int) = 
    new RuntimeException(message)

  protected var autoVerify = true
}

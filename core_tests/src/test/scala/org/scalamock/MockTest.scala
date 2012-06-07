// Copyright (c) 2011-2012 Paul Butcher
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

package org.scalamock

import org.scalamock.scalatest.MockFactory
import org.scalatest.FreeSpec

class MockTest extends FreeSpec with MockFactory {
  
  autoVerify = false
  
  trait TestTrait {
    def nullary: String
    def noParams(): String
    def oneParam(x: Int): String
    def twoParams(x: Int, y: Double): String
    
    def overloaded(x: Int): String
    def overloaded(x: String): String
    def overloaded(x: Int, y: Double): String
    
    def +(x: TestTrait): TestTrait
    
    def curried(x: Int)(y: Double): String
    def polymorphic[T](x: T): String
    def polycurried[T1, T2](x: T1)(y: T2): String
    def polymorphicParam(x: (Int, Double)): String
    def repeatedParam(x: Int, ys: String*): String

    def byNameParam(x: => Int): String
  }
  
  "Mocks should" - {
    "fail if an unexpected method call is made" in {
      withExpectations {
        val m = mock[TestTrait]
        intercept[ExpectationException] { m.oneParam(42) }
      }
    }
    
    "allow expectations to be set" in {
      withExpectations {
        val m = mock[TestTrait]
        (m.twoParams _).expects(42, 1.23).returning("a return value")
        expect("a return value") { m.twoParams(42, 1.23) }
      }
    }
    
    "cope with nullary methods" in {
      withExpectations {
        val m = mock[TestTrait]
        (m.nullary _).expects().returning("a return value")
        expect("a return value") { m.nullary }
      }
    }
    
    "cope with overloaded methods" in {
      withExpectations {
        val m = mock[TestTrait]
        (m.overloaded(_: Int)).expects(10).returning("got an integer")
        (m.overloaded(_: Int, _: Double)).expects(10, 1.23).returning("got two parameters")
        expect("got an integer") { m.overloaded(10) }
        expect("got two parameters") { m.overloaded(10, 1.23) }
      }
    }
    
    "cope with infix operators" in {
      withExpectations {
        val m1 = mock[TestTrait]
        val m2 = mock[TestTrait]
        val m3 = mock[TestTrait]
        (m1.+ _).expects(m2).returning(m3)
        expect(m3) { m1 + m2 }
      }
    }
    
    "cope with curried methods" in {
      withExpectations {
        val m = mock[TestTrait]
        (m.curried(_: Int)(_: Double)).expects(10, 1.23).returning("curried method called")
        val partial = m.curried(10) _
        expect("curried method called") { partial(1.23) }
      }
    }
    
    "cope with polymorphic methods" in {
      withExpectations {
        val m = mock[TestTrait]
        (m.polymorphic(_: Int)).expects(42).returning("called with integer")
        (m.polymorphic(_: String)).expects("foo").returning("called with string")
        expect("called with integer") { m.polymorphic(42) }
        expect("called with string") { m.polymorphic("foo") }
      }
    }
    
    "cope with curried polymorphic methods" in {
      withExpectations {
        val m = mock[TestTrait]
        (m.polycurried(_: Int)(_: String)).expects(42, "foo").returning("it works")
        val partial = m.polycurried(42) _
        expect("it works") { partial("foo") }
      }
    }
    
    "cope with parameters of polymorphic type" in {
      withExpectations {
        val m = mock[TestTrait]
        (m.polymorphicParam _).expects((42, 1.23)).returning("it works")
        expect("it works") { m.polymorphicParam((42, 1.23)) }
      }
    }

    "cope with methods with repeated parameters" in {
      withExpectations {
        val m = mock[TestTrait]
        val f: (Int, String*) => String = m.repeatedParam _
        (m.repeatedParam _).expects(42, Seq("foo", "bar"))
        m.repeatedParam(42, "foo", "bar")
      }
    }
    
    "cope with methods with by name parameters" in {
      withExpectations {
        val m = mock[TestTrait]
        (m.byNameParam _).expects(*).returning("it worked")
        expect("it worked") { m.byNameParam(42) }
      }
    }
    
    //! TODO - find a way to make this less ugly
    "match methods with by name parameters" in {
      withExpectations {
        val m = mock[TestTrait]
        val f: (=> Int) => Boolean = { x => x == 1 && x == 2  }
        ((m.byNameParam _): (=> Int) => String).expects(new FunctionAdapter1(f)).returning("it works")
        var y = 0
        expect("it works") { m.byNameParam { y += 1; y } }
      }
    }
  }
  
  "Stubs should" - {
    "return null unless told otherwise" in {
      withExpectations {
        val m = stub[TestTrait]
        expect(null) { m.oneParam(42) }
      }
    }
    
    "return what they're told to" in {
      withExpectations {
        val m = stub[TestTrait]
        (m.twoParams _).when(42, 1.23).returns("a return value")
        expect("a return value") { m.twoParams(42, 1.23) }
      }
    }
    
    "verify calls" in {
      withExpectations {
        val m = stub[TestTrait]
        m.twoParams(42, 1.23)
        m.twoParams(42, 1.23)
        (m.twoParams _).verify(42, 1.23).twice
      }
    }
    
    "fail when verification fails" in {
      intercept[ExpectationException](withExpectations {
        val m = stub[TestTrait]
        m.twoParams(42, 1.00)
        (m.twoParams _).verify(42, 1.23).once
      })
    }
  }
}
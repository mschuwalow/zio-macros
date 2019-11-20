/*
 * Copyright 2017-2019 John A. De Goes and the ZIO Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package zio.macros.delegate

import scala.deriving._
import scala.quoted._

/**
 * Evidence that two instances can be mixed.
 */
trait Mix[A, B]
  def mix(a: A, b: B): A & B

object Mix
  inline given derived[A, B]: Mix[A, B] =
    ${ Macros.deriveMix[A, B] }

trait Foo1
    def foo1 = 3
class Foo() extends Foo1
trait Bar

object Foo
  val foo = Mix.derived[Foo, Bar]

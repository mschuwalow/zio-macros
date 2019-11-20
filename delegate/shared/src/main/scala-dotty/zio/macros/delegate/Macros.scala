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

object Macros
  def deriveMix[A: Type, B: Type](given qctx: QuoteContext): Expr[Mix[A, B]] =
    import qctx.tasty.{_, given}
    val tpeA = typeOf[A]
    val flagsA = tpeA.typeSymbol.flags

    val tpeB = typeOf[B]
    val flagsB = tpeB.typeSymbol.flags

    // fail compilation if preconditions are violated.
    check(
      (!flagsA.is(Flags.Final), s"${tpeA.show} must be a nonfinal class or trait."),
      (flagsB.is(Flags.Trait), s"${tpeB.show} must be a trait.")
    )
    val smb: Symbol = tpeA.classSymbol.get
    println(smb.baseClasses)
    ???

  def check(conds: (Boolean, String)*)(given qctx: QuoteContext): Unit =
    conds.foreach { (cond, msg) =>
      if (!cond) qctx.error(msg)
    }

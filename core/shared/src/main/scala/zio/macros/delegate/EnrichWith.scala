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

import zio._

final class EnrichWith[B](val value: B) {

  def apply[A](a: A)(implicit ev: A Mix B): A with B =
    ev.mix(a, value)

  def enrichZIO[R, E, A](zio: ZIO[R, E, A])(implicit ev: A Mix B): ZIO[R, E, A with B] = zio.map(ev.mix(_, value))

  def enrichZManaged[R, E, A](zManaged: ZManaged[R, E, A])(implicit ev: A Mix B): ZManaged[R, E, A with B] =
    zManaged.map(ev.mix(_, value))

  val toEnrichWithM: EnrichWithM[Any, Nothing, B] =
    new EnrichWithM(ZIO.succeed(value))

  val toEnrichWithManaged: EnrichWithManaged[Any, Nothing, B] =
    new EnrichWithManaged(ZManaged.succeed(value))
}

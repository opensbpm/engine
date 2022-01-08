/*******************************************************************************
 * Copyright (C) 2020 Stefan Sedelmaier
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.opensbpm.engine.core.model.entities;

import java.util.Optional;

public interface StateVisitor<T> {

    T visitFunctionState(FunctionState functionState);

    T visitReceiveState(ReceiveState receiveState);

    T visitSendState(SendState sendState);

    static StateVisitor<Optional<FunctionState>> functionState() {
        return new OptionalStateAdapter<FunctionState>() {
            @Override
            public Optional<FunctionState> visitFunctionState(FunctionState state) {
                return Optional.of(state);
            }
        };
    }

    static StateVisitor<Optional<ReceiveState>> receiveState() {
        return new OptionalStateAdapter<ReceiveState>() {
            @Override
            public Optional<ReceiveState> visitReceiveState(ReceiveState state) {
                return Optional.of(state);
            }
        };
    }

    static StateVisitor<Optional<SendState>> sendState() {
        return new OptionalStateAdapter<SendState>() {
            @Override
            public Optional<SendState> visitSendState(SendState state) {
                return Optional.of(state);
            }
        };
    }

    public static class OptionalStateAdapter<T> implements StateVisitor<Optional<T>> {

        @Override
        public Optional<T> visitFunctionState(FunctionState functionState) {
            return Optional.empty();
        }

        @Override
        public Optional<T> visitReceiveState(ReceiveState receiveState) {
            return Optional.empty();
        }

        @Override
        public Optional<T> visitSendState(SendState sendState) {
            return Optional.empty();
        }
    }
}

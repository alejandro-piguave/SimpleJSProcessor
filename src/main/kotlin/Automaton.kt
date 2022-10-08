
class Automaton {
    private val finalStates: Map<State, Token> = mapOf(
        State.Q1 to Token.IDENTIFIER, State.Q3 to Token.STRING, State.Q4 to Token.INTEGER, State.Q4 to Token.FLOAT )

    private fun executeTransition(currentState: State, entry: Char): State {
        return when (currentState) {
            State.INITIAL -> {
                when (entry) {
                    in 'A'..'Z', in 'a'..'z' -> State.Q1
                    '"' -> State.Q2
                    in '0'..'9' -> State.Q4
                    '+', '-' -> State.Q5
                    else -> State.INVALIDATION_STATE
                }
            }
            State.Q1 -> {
                if (entry in 'A'..'Z' || entry in 'a'..'z' || entry in '0'..'9') State.Q1
                else State.INVALIDATION_STATE
            }
            State.Q2 -> {
                if (entry == '"') State.Q3
                else State.Q2
            }
            State.Q4 -> {
                when (entry) {
                    '.' -> State.Q6
                    in '0'..'9' -> State.Q4
                    else -> State.INVALIDATION_STATE
                }
            }
            State.Q5 -> {
                if (entry in '0'..'9') State.Q4
                else State.INVALIDATION_STATE
            }
            State.Q6, State.Q7 -> {
                if (entry in '0'..'9') State.Q7
                else State.INVALIDATION_STATE
            }
            else -> State.INVALIDATION_STATE
        }
    }

    fun evaluate(str: String): Token {
        var state = State.INITIAL
        for (c in str.toCharArray()) {
            state = executeTransition(state, c)
        }
        return finalStates.getOrDefault(state, Token.INVALID)
    }
}
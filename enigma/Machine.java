package enigma;

import java.util.ArrayList;
import java.util.Collection;

import static enigma.EnigmaException.*;

/** Class that represents a complete enigma machine.
 *  @author Yunsu Ha
 */
class Machine {

    /** A new Enigma machine with alphabet ALPHA, 1 < NUMROTORS rotor slots,
     *  and 0 <= PAWLS < NUMROTORS pawls.  ALLROTORS contains all the
     *  available rotors. */
    Machine(Alphabet alpha, int numRotors, int pawls,
            Collection<Rotor> allRotors) {
        _alphabet = alpha;
        _allRotors = new ArrayList<>(allRotors);
        _numRotors = numRotors;
        _pawls = pawls;
        _rotorSlots = new Rotor[numRotors];
    }

    /** Return the number of rotor slots I have. */
    int numRotors() {
        return _numRotors;
    }

    /** Return the number pawls (and thus rotating rotors) I have. */
    int numPawls() {
        return _pawls;
    }

    /** Return Rotor #K, where Rotor #0 is the reflector, and Rotor
     *  #(numRotors()-1) is the fast Rotor.  Modifying this Rotor has
     *  undefined results. */
    Rotor getRotor(int k) {
        return _rotorSlots[k];
    }

    Alphabet alphabet() {
        return _alphabet;
    }

    /** Set my rotor slots to the rotors named ROTORS from my set of
     *  available rotors (ROTORS[0] names the reflector).
     *  Initially, all rotors are set at their 0 setting. */
    void insertRotors(String[] rotors) {
        for (int i = 0; i < rotors.length; i++) {
            for (int j = 0; j < _allRotors.size(); j++) {
                if (rotors[i].equals(_allRotors.get(j).name())) {
                    _rotorSlots[i] = _allRotors.get(j);
                }
            }
        }
    }

    /** Set my rotors according to SETTING, which must be a string of
     *  numRotors()-1 characters in my alphabet. The first letter refers
     *  to the leftmost rotor setting (not counting the reflector).  */
    void setRotors(String setting) {
        char[] charSetting = setting.toCharArray();
        for (int i = 1; i < numRotors(); i++) {
            _rotorSlots[i].set(charSetting[i - 1]);
        }
    }

    /** Return the current plugboard's permutation. */
    Permutation plugboard() {
        return _plugboard;
    }

    /** Set the plugboard to PLUGBOARD. */
    void setPlugboard(Permutation plugboard) {
        _plugboard = plugboard;
    }

    /** Returns the result of converting the input character C (as an
     *  index in the range 0..alphabet size - 1), after first advancing
     *  the machine. */
    int convert(int c) {
        advanceRotors();
        if (Main.verbose()) {
            System.err.printf("[");
            for (int r = 1; r < numRotors(); r += 1) {
                System.err.printf("%c",
                        alphabet().toChar(getRotor(r).setting()));
            }
            System.err.printf("] %c -> ", alphabet().toChar(c));
        }
        c = plugboard().permute(c);
        if (Main.verbose()) {
            System.err.printf("%c -> ", alphabet().toChar(c));
        }
        c = applyRotors(c);
        c = plugboard().permute(c);
        if (Main.verbose()) {
            System.err.printf("%c%n", alphabet().toChar(c));
        }
        return c;
    }

    /** advances the rotors. */
    private void advanceRotors() {
        boolean nextAtNotch = false;
        for (int i = numRotors() - numPawls(); i < numRotors() - 1; i++) {
            if (_rotorSlots[i + 1].atNotch()) {
                _rotorSlots[i].advance();
                nextAtNotch = true;
            } else if (nextAtNotch) {
                _rotorSlots[i].advance();
                nextAtNotch = false;
            }
        }
        _rotorSlots[numRotors() - 1].advance();
    }

    /** Return the result of applying the rotors to the character C (as an
     *  index in the range 0..alphabet size - 1). */
    private int applyRotors(int c) {
        for (int i = numRotors() - 1; i >= 0; i--) {
            c = _rotorSlots[i].convertForward(c);
        }
        for (int i = 1; i < numRotors(); i++) {
            c = _rotorSlots[i].convertBackward(c);
        }
        return c;
    }

    /** Returns the encoding/decoding of MSG, updating the state of
     *  the rotors accordingly. */
    String convert(String msg) {
        String returnVal = "";
        for (int i = 0; i < msg.length(); i++) {
            char msgParts = msg.charAt(i);
            int converted = convert(alphabet().toInt(msgParts));
            char temp = alphabet().toChar(converted);
            returnVal += temp;
        }
        return returnVal;
    }

    /** Returns _allRotors. */
    ArrayList<Rotor> allRotors() {
        return _allRotors;
    }

    /** returns _rotorSlots. */
    Rotor[] rotorSlots() {
        return _rotorSlots;
    }

    /** Common alphabet of my rotors. */
    private final Alphabet _alphabet;

    /** all the rotors from the conf file. */
    private java.util.ArrayList<Rotor> _allRotors;

    /** the rotors that are slotted in. */
    private Rotor[] _rotorSlots;

    /** the number of rotors placed. */
    private int _numRotors;

    /** the number of pawls placed. */
    private int _pawls;

    /** the plugboard. */
    private Permutation _plugboard;

}

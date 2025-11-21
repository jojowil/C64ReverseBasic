import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class C64ReverseBasic {

    static Map<Byte,String> keywords = new HashMap<>();
    static Map<Byte,String> keycodes = new HashMap<>();

    // Populate the keycodes map.
    private static void populateKeycodes() {
        keycodes.put((byte)0x05, "{white}");
        keycodes.put((byte)0x0d, "{return}");
        keycodes.put((byte)0x11, "{down}");
        keycodes.put((byte)0x12, "{reverse on}");
        keycodes.put((byte)0x13, "{home}");
        keycodes.put((byte)0x14, "{delete}");
        keycodes.put((byte)0x1c, "{red}");
        keycodes.put((byte)0x1d, "{right}");
        keycodes.put((byte)0x1e, "{green}");
        keycodes.put((byte)0x1f, "{blue}");
        keycodes.put((byte)0x5c, "{pound}");
        keycodes.put((byte)0x81, "{orange}");
        keycodes.put((byte)0x90, "{black}");
        keycodes.put((byte)0x91, "{up}");
        keycodes.put((byte)0x92, "{reverse off}");
        keycodes.put((byte)0x93, "{clear}");
        keycodes.put((byte)0x95, "{brown}");
        keycodes.put((byte)0x96, "{pink}");
        keycodes.put((byte)0x97, "{dark gray}");
        keycodes.put((byte)0x98, "{grey}");
        keycodes.put((byte)0x99, "{light green}");
        keycodes.put((byte)0x9a, "{light blue}");
        keycodes.put((byte)0x9b, "{light gray}");
        keycodes.put((byte)0x9c, "{purple}");
        keycodes.put((byte)0x9d, "{left}");
        keycodes.put((byte)0x9e, "{yellow}");
        keycodes.put((byte)0x9f, "{cyan}");
        keycodes.put((byte)0xff, "{pi}");
    }

    // Populate the keywords map.
    private static void populateKeywords(){
        keywords.put((byte)0x80, "end");
        keywords.put((byte)0x81, "for");
        keywords.put((byte)0x82, "next");
        keywords.put((byte)0x83, "data");
        keywords.put((byte)0x84, "input#");
        keywords.put((byte)0x85, "input");
        keywords.put((byte)0x86, "dim");
        keywords.put((byte)0x87, "read");
        keywords.put((byte)0x88, "let");
        keywords.put((byte)0x89, "goto");
        keywords.put((byte)0x8a, "run");
        keywords.put((byte)0x8b, "if");
        keywords.put((byte)0x8c, "restore");
        keywords.put((byte)0x8d, "gosub");
        keywords.put((byte)0x8e, "return");
        keywords.put((byte)0x8f, "rem");
        keywords.put((byte)0x90, "stop");
        keywords.put((byte)0x91, "on");
        keywords.put((byte)0x92, "wait");
        keywords.put((byte)0x93, "load");
        keywords.put((byte)0x94, "save");
        keywords.put((byte)0x95, "verify");
        keywords.put((byte)0x96, "def");
        keywords.put((byte)0x97, "poke");
        keywords.put((byte)0x98, "print#");
        keywords.put((byte)0x99, "print");
        keywords.put((byte)0x9a, "cont");
        keywords.put((byte)0x9b, "list");
        keywords.put((byte)0x9c, "clr");
        keywords.put((byte)0x9d, "cmd");
        keywords.put((byte)0x9e, "sys");
        keywords.put((byte)0x9f, "open");
        keywords.put((byte)0xa0, "close");
        keywords.put((byte)0xa1, "get");
        keywords.put((byte)0xa2, "new");
        keywords.put((byte)0xa3, "tab(");
        keywords.put((byte)0xa4, "to");
        keywords.put((byte)0xa5, "fn");
        keywords.put((byte)0xa6, "spc(");
        keywords.put((byte)0xa7, "then");
        keywords.put((byte)0xa8, "not");
        keywords.put((byte)0xa9, "step");
        keywords.put((byte)0xaa, "+");
        keywords.put((byte)0xab, "-");
        keywords.put((byte)0xac, "*");
        keywords.put((byte)0xad, "/");
        //keywords.put((byte)0xae, "↑");
        keywords.put((byte)0xae, "^");
        keywords.put((byte)0xaf, "and");
        keywords.put((byte)0xb0, "or");
        keywords.put((byte)0xb1, ">");
        keywords.put((byte)0xb2, "=");
        keywords.put((byte)0xb3, "<");
        keywords.put((byte)0xb4, "sgn");
        keywords.put((byte)0xb5, "int");
        keywords.put((byte)0xb6, "abs");
        keywords.put((byte)0xb7, "usr");
        keywords.put((byte)0xb8, "fre");
        keywords.put((byte)0xb9, "pos");
        keywords.put((byte)0xba, "sqr");
        keywords.put((byte)0xbb, "rnd");
        keywords.put((byte)0xbc, "log");
        keywords.put((byte)0xbd, "exp");
        keywords.put((byte)0xbe, "cos");
        keywords.put((byte)0xbf, "sin");
        keywords.put((byte)0xc0, "tan");
        keywords.put((byte)0xc1, "atn");
        keywords.put((byte)0xc2, "peek");
        keywords.put((byte)0xc3, "len");
        keywords.put((byte)0xc4, "str$");
        keywords.put((byte)0xc5, "val");
        keywords.put((byte)0xc6, "asc");
        keywords.put((byte)0xc7, "chr$");
        keywords.put((byte)0xc8, "left$");
        keywords.put((byte)0xc9, "right$");
        keywords.put((byte)0xca, "mid$");
        keywords.put((byte)0xcb, "go");
    }

    // Convert to PETSCII
    private static char petscii(char c) {
        if ( c >= 'A' && c <= 'Z' ) return (char)(c + 32);
        if ( c >= 'a' && c <= 'z' ) return (char)(c - 32);
        return c;
    }

    // A Sample hexdump (https://programmingby.design/algorithms/the-hex-dump/)
    private static void hexdump(byte[] bytes) {
        int start = (bytes[1] & 0xff) * 256 + (bytes[0] & 0xff);
        int pc = start;
        StringBuilder chars = new StringBuilder();
        int zeroes = 0;

        for (int x = 2; x < bytes.length; x++) {
            if ( (pc - start) % 8 == 0 ) {
                System.out.printf(" %s", chars);
                if ( zeroes >= 3 ) return;
                System.out.printf("\n%04X:", pc);
                chars.setLength(0);
            }
            char c = (char)(bytes[x] & 0xff);
            zeroes = c==0 ? zeroes + 1 : 0;
            if ( c >= 32 && c <= 127 )
                chars.append(c);
            else
                chars.append('.');
            System.out.printf(" %02X", bytes[x]);
            pc++;
        }
        // fix final row
        int last = (pc - start) % 8;
        if (last > 0)
            for (int x = 0; x < 8-last; x++)
                System.out.print("   ");
        System.out.printf(" %s\n", chars);
    }

    // Reverse engineer the tokenized data back into the original code.
    public static void basic(byte[] bytes) {
        int pc = (bytes[1] & 0xff) * 256 + (bytes[0] & 0xff);
        int bal = bytes.length, next, lnum;

        System.out.printf("start: 0x%04X%n", pc);
        for ( int x = 2; x < bal; x++ ) {
            // next address in the linked list.
            next = (bytes[x++] & 0xff) + (bytes[x++] & 0xff) * 256;
            //System.out.printf("next: 0x%04X%n", next);

            // final pointer is 0x0000 indicating the end.
            if ( next == 0 ) {
                //System.out.println("END OF PROGRAM!");
                return;
            }
            // line number is after next pointer.
            lnum = (bytes[x++] & 0xff) + (bytes[x++] & 0xff) * 256;
            //System.out.printf("lnum: 0x%04X%n", lnum);
            System.out.print(lnum + " ");

            // early test to walk the links and line numbers.
            //while ((bytes[x++] & 0xff) != 0); //debug
            //x--; //debug

            boolean inQuotes = false;
            // now we classify the byte until we reach a 0x00 (end of line).
            while (x < bal && bytes[x] != 0) {
                int cur = bytes[x] & 0xff;

                if (cur == '\"')
                    inQuotes = !inQuotes;
                // token?
                if ( (cur & 0x80) > 0  && !inQuotes) {
                    if (keywords.containsKey((byte)cur))
                        System.out.print(keywords.get((byte)cur));
                } else {
                    if (keycodes.containsKey((byte)cur))
                        System.out.print(keycodes.get((byte)cur));
                    else
                        System.out.print(petscii((char)cur));
                }
                x++;
            }
            System.out.println();
        }
    }

    public static void main(String[] args) throws IOException {

        FileInputStream in=null;

        try {
            in = new FileInputStream("examples/coderain.prg");
        } catch (FileNotFoundException e) {
            System.out.println("Cannot open input file. Terminating.");
            System.exit(1);
        }

        byte[] file = in.readAllBytes();
        in.close();

        populateKeycodes();
        populateKeywords();

        hexdump(file);
        basic(file);
    }
}

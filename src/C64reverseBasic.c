#include <stdio.h>
#include <stdlib.h>

static char *containsKeyword(int code);

static char *containsKeycode(int code);

static char petscii(char c);

static void hexDump(const unsigned char *bytes, long length);

static void basic(const unsigned char* bytes, long len);

static unsigned char *readAllBytes(const char *fname, long *len);

// keyword/keycode struct.
typedef struct {
    unsigned char val;
    char *name;
} Keyword;

    // Populate the keycodes map.
Keyword keycodes[] = {
    {0x05, "{white}"},
    {0x0d, "{return}"},
    {0x11, "{down}"},
    {0x12, "{reverse on}"},
    {0x13, "{home}"},
    {0x14, "{delete}"},
    {0x1c, "{red}"},
    {0x1d, "{right}"},
    {0x1e, "{green}"},
    {0x1f, "{blue}"},
    {0x5c, "{pound}"},
    {0x81, "{orange}"},
    {0x90, "{black}"},
    {0x91, "{up}"},
    {0x92, "{reverse off}"},
    {0x93, "{clear}"},
    {0x95, "{brown}"},
    {0x96, "{pink}"},
    {0x97, "{dark gray}"},
    {0x98, "{grey}"},
    {0x99, "{light green}"},
    {0x9a, "{light blue}"},
    {0x9b, "{light gray}"},
    {0x9c, "{purple}"},
    {0x9d, "{left}"},
    {0x9e, "{yellow}"},
    {0x9f, "{cyan}"},
    {0xff, "{pi}"}
};
int keycodeLength = sizeof(keycodes) / sizeof(keycodes[0]);

// Populate the keywords map.
Keyword keywords[] = {
    {0x80, "end"},
    {0x81, "for"},
    {0x82, "next"},
    {0x83, "data"},
    {0x84, "input#"},
    {0x85, "input"},
    {0x86, "dim"},
    {0x87, "read"},
    {0x88, "let"},
    {0x89, "goto"},
    {0x8a, "run"},
    {0x8b, "if"},
    {0x8c, "restore"},
    {0x8d, "gosub"},
    {0x8e, "return"},
    {0x8f, "rem"},
    {0x90, "stop"},
    {0x91, "on"},
    {0x92, "wait"},
    {0x93, "load"},
    {0x94, "save"},
    {0x95, "verify"},
    {0x96, "def"},
    {0x97, "poke"},
    {0x98, "print#"},
    {0x99, "print"},
    {0x9a, "cont"},
    {0x9b, "list"},
    {0x9c, "clr"},
    {0x9d, "cmd"},
    {0x9e, "sys"},
    {0x9f, "open"},
    {0xa0, "close"},
    {0xa1, "get"},
    {0xa2, "new"},
    {0xa3, "tab("},
    {0xa4, "to"},
    {0xa5, "fn"},
    {0xa6, "spc("},
    {0xa7, "then"},
    {0xa8, "not"},
    {0xa9, "step"},
    {0xaa, "+"},
    {0xab, "-"},
    {0xac, "*"},
    {0xad, "/"},
    {0xae, "↑"},
    {0xae, "^"},
    {0xaf, "and"},
    {0xb0, "or"},
    {0xb1, ">"},
    {0xb2, "="},
    {0xb3, "<"},
    {0xb4, "sgn"},
    {0xb5, "int"},
    {0xb6, "abs"},
    {0xb7, "usr"},
    {0xb8, "fre"},
    {0xb9, "pos"},
    {0xba, "sqr"},
    {0xbb, "rnd"},
    {0xbc, "log"},
    {0xbd, "exp"},
    {0xbe, "cos"},
    {0xbf, "sin"},
    {0xc0, "tan"},
    {0xc1, "atn"},
    {0xc2, "peek"},
    {0xc3, "len"},
    {0xc4, "str$"},
    {0xc5, "val"},
    {0xc6, "asc"},
    {0xc7, "chr$"},
    {0xc8, "left$"},
    {0xc9, "right$"},
    {0xca, "mid$"},
    {0xcb, "go"}
};
int keywordLength = sizeof(keywords) / sizeof(keywords[0]);

// Find keyword string
static char *containsKeyword(int code) {
    for (int x = 0; x < keywordLength; x++) {
        if (keywords[x].val == code)
            return keywords[x].name;
    }
    return NULL;
}

// Find keycode string
static char *containsKeycode(int code) {
    for (int x = 0; x < keycodeLength; x++) {
        if (keycodes[x].val == code)
            return keycodes[x].name;
    }
    return NULL;
}

// Convert to PETSCII
static char petscii(char c) {
    if ( c >= 'A' && c <= 'Z' ) return (char)(c + 32);
    if ( c >= 'a' && c <= 'z' ) return (char)(c - 32);
    return c;
}

// A Sample hexdump (https://programmingby.design/algorithms/the-hex-dump/)
static void hexDump(const unsigned char *bytes, long length) {
    // we and with 0xff to mask off the bits when sign-extended.
    if (length == 0 || bytes == NULL) return;

    int st = bytes[1] * 256 + bytes[0];
    printf("%d\n", st);
    int pc = st, c = 0, x;
    char chars[10] = {0};

    for (x = 2; x < length; x++) {
        // make it pretty
        if ((pc - st) % 8 == 0) {
            // chars are built during each iteration, but
            // printed when we reach the end of the line.
            printf(" %s", chars);
            printf("\n%04X:", pc);
            c = 0;
        }
        unsigned char ch = bytes[x];
        // build the chars - only printable chars
        chars[c++] = (ch >= 32 && ch <= 127) ? ch : '.';

        printf(" %02X", bytes[x]);
        pc++;
    }

    // fix final row
    int last = (pc - st) % 8;
    chars[c] = '\0';
    if (last > 0)
        for (x = 0; x < 8 - last; x++)
            printf("   ");
    printf(" %s\n", chars);
}

// Reverse engineer the tokenized data back into the original code.
static void basic(const unsigned char* bytes, long len) {
    if (bytes == NULL || len == 0) return;

    int pc = (bytes[1] & 0xff) * 256 + (bytes[0] & 0xff);
    printf("start: 0x%04X\n", pc);
    for ( int x = 2; x < len; x++ ) {
        // next address in the linked list.
        int next = (bytes[x++] & 0xff) + (bytes[x++] & 0xff) * 256;
        //System.out.printf("next: 0x%04X%n", next);

        // final pointer is 0x0000 indicating the end.
        if ( next == 0 ) {
            //System.out.println("END OF PROGRAM!");
            return;
        }
        // line number is after next pointer.
        int lnum = (bytes[x++] & 0xff) + (bytes[x++] & 0xff) * 256;
        //System.out.printf("lnum: 0x%04X%n", lnum);
        printf("%d ",lnum);

        // early test to walk the links and line numbers.
        //while ((bytes[x++] & 0xff) != 0); //debug
        //x--; //debug

        int inQuotes = 0;
        // now we classify the byte until we reach a 0x00 (end of line).
        while (x < len && bytes[x] != 0) {
            int cur = bytes[x] & 0xff;

            if (cur == '\"')
                inQuotes = !inQuotes;
            // token?
            char *key;
            if ( (cur & 0x80) > 0  && !inQuotes) {
                if ((key = containsKeyword(cur)) != NULL)
                    printf("%s", key);
            } else {
                if ((key = containsKeycode(cur)) != NULL)
                    printf("%s", key);
                else
                    printf("%c", petscii((char)cur));
            }
            x++;
        }
        printf("\n");
    }
}

// Read a complete file into memory.
static unsigned char *readAllBytes(const char *fname, long *len) {
    // open file
    FILE *file = fopen(fname, "r");
    if (file == NULL) {
        perror("Error opening source file");
        return NULL;
    }

    // get size
    fseek(file, 0, SEEK_END);
    long fileSize = ftell(file);
    rewind(file);

    // get the mem
    unsigned char *buffer = malloc(fileSize * sizeof(char) + 1);

    // Read the entire file into the buffer
    size_t bytesRead = fread(buffer, 1, fileSize, file);
    if (bytesRead != fileSize) {
        perror("Error reading file");
        fclose(file);
        free(buffer);
        *len = 0;
        return NULL;
    }
    // add the terminator
    buffer[fileSize] = '\0';
    *len = fileSize; // be sure to note the size!
    return buffer;
}

int main(int argc, char **argv) {
    // check for program file
    if (argc != 2) {
        printf("\nMust provide object file.\n\n");
        exit(3);
    }

    // Read file contents
    long flen;
    unsigned char* file = readAllBytes(argv[1], &flen);

    // Display our findings
    hexDump(file, flen);
    printf("\n");
    basic(file, flen);
    free(file);
}

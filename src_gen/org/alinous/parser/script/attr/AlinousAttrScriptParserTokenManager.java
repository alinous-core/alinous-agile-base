/* Generated By:JavaCC: Do not edit this line. AlinousAttrScriptParserTokenManager.java */
package org.alinous.parser.script.attr;

@SuppressWarnings({"unused"})
public class AlinousAttrScriptParserTokenManager implements AlinousAttrScriptParserConstants
{
  public  java.io.PrintStream debugStream = System.out;
  public  void setDebugStream(java.io.PrintStream ds) { debugStream = ds; }
private final int jjStopAtPos(int pos, int kind)
{
   jjmatchedKind = kind;
   jjmatchedPos = pos;
   return pos + 1;
}
private final int jjMoveStringLiteralDfa0_5()
{
   switch(curChar)
   {
      case 60:
         return jjStopAtPos(0, 8);
      default :
         return 1;
   }
}
private final int jjMoveStringLiteralDfa0_4()
{
   switch(curChar)
   {
      case 62:
         return jjStopAtPos(0, 10);
      case 92:
         return jjMoveStringLiteralDfa1_4(0x800L);
      case 123:
         return jjStopAtPos(0, 9);
      default :
         return 1;
   }
}
private final int jjMoveStringLiteralDfa1_4(long active0)
{
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      return 1;
   }
   switch(curChar)
   {
      case 123:
         if ((active0 & 0x800L) != 0L)
            return jjStopAtPos(1, 11);
         break;
      default :
         return 2;
   }
   return 2;
}
private final int jjMoveStringLiteralDfa0_2()
{
   switch(curChar)
   {
      case 9:
         jjmatchedKind = 19;
         return jjMoveNfa_2(0, 0);
      case 10:
         jjmatchedKind = 20;
         return jjMoveNfa_2(0, 0);
      case 13:
         jjmatchedKind = 21;
         return jjMoveNfa_2(0, 0);
      case 32:
         jjmatchedKind = 18;
         return jjMoveNfa_2(0, 0);
      case 33:
         jjmatchedKind = 45;
         return jjMoveStringLiteralDfa1_2(0x1000000L);
      case 34:
         jjmatchedKind = 54;
         return jjMoveNfa_2(0, 0);
      case 36:
         jjmatchedKind = 22;
         return jjMoveNfa_2(0, 0);
      case 37:
         jjmatchedKind = 40;
         return jjMoveNfa_2(0, 0);
      case 38:
         return jjMoveStringLiteralDfa1_2(0x20000000000L);
      case 39:
         jjmatchedKind = 55;
         return jjMoveNfa_2(0, 0);
      case 40:
         jjmatchedKind = 31;
         return jjMoveNfa_2(0, 0);
      case 41:
         jjmatchedKind = 32;
         return jjMoveNfa_2(0, 0);
      case 42:
         jjmatchedKind = 34;
         return jjMoveNfa_2(0, 0);
      case 43:
         jjmatchedKind = 36;
         return jjMoveStringLiteralDfa1_2(0x800000000L);
      case 44:
         jjmatchedKind = 43;
         return jjMoveNfa_2(0, 0);
      case 45:
         jjmatchedKind = 38;
         return jjMoveStringLiteralDfa1_2(0x2000000000L);
      case 46:
         jjmatchedKind = 33;
         return jjMoveNfa_2(0, 0);
      case 47:
         jjmatchedKind = 39;
         return jjMoveNfa_2(0, 0);
      case 60:
         jjmatchedKind = 27;
         return jjMoveStringLiteralDfa1_2(0x10000000L);
      case 61:
         return jjMoveStringLiteralDfa1_2(0x800000L);
      case 62:
         jjmatchedKind = 25;
         return jjMoveStringLiteralDfa1_2(0x4000000L);
      case 64:
         jjmatchedKind = 44;
         return jjMoveNfa_2(0, 0);
      case 66:
         return jjMoveStringLiteralDfa1_2(0x2000000000000L);
      case 70:
         return jjMoveStringLiteralDfa1_2(0x1000000000000L);
      case 73:
         return jjMoveStringLiteralDfa1_2(0x4000000000000L);
      case 78:
         return jjMoveStringLiteralDfa1_2(0x8000000000000L);
      case 84:
         return jjMoveStringLiteralDfa1_2(0x800000000000L);
      case 91:
         jjmatchedKind = 29;
         return jjMoveNfa_2(0, 0);
      case 93:
         jjmatchedKind = 30;
         return jjMoveNfa_2(0, 0);
      case 98:
         return jjMoveStringLiteralDfa1_2(0x2000000000000L);
      case 102:
         return jjMoveStringLiteralDfa1_2(0x1000000000000L);
      case 105:
         return jjMoveStringLiteralDfa1_2(0x4000000000000L);
      case 110:
         return jjMoveStringLiteralDfa1_2(0x8000000000000L);
      case 116:
         return jjMoveStringLiteralDfa1_2(0x800000000000L);
      case 124:
         return jjMoveStringLiteralDfa1_2(0x40000000000L);
      case 125:
         jjmatchedKind = 46;
         return jjMoveNfa_2(0, 0);
      default :
         return jjMoveNfa_2(0, 0);
   }
}
private final int jjMoveStringLiteralDfa1_2(long active0)
{
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
   return jjMoveNfa_2(0, 0);
   }
   switch(curChar)
   {
      case 38:
         if ((active0 & 0x20000000000L) != 0L)
         {
            jjmatchedKind = 41;
            jjmatchedPos = 1;
         }
         break;
      case 43:
         if ((active0 & 0x800000000L) != 0L)
         {
            jjmatchedKind = 35;
            jjmatchedPos = 1;
         }
         break;
      case 45:
         if ((active0 & 0x2000000000L) != 0L)
         {
            jjmatchedKind = 37;
            jjmatchedPos = 1;
         }
         break;
      case 61:
         if ((active0 & 0x800000L) != 0L)
         {
            jjmatchedKind = 23;
            jjmatchedPos = 1;
         }
         else if ((active0 & 0x1000000L) != 0L)
         {
            jjmatchedKind = 24;
            jjmatchedPos = 1;
         }
         else if ((active0 & 0x4000000L) != 0L)
         {
            jjmatchedKind = 26;
            jjmatchedPos = 1;
         }
         else if ((active0 & 0x10000000L) != 0L)
         {
            jjmatchedKind = 28;
            jjmatchedPos = 1;
         }
         break;
      case 65:
         return jjMoveStringLiteralDfa2_2(active0, 0x1000000000000L);
      case 70:
         if ((active0 & 0x4000000000000L) != 0L)
         {
            jjmatchedKind = 50;
            jjmatchedPos = 1;
         }
         break;
      case 79:
         return jjMoveStringLiteralDfa2_2(active0, 0x2000000000000L);
      case 82:
         return jjMoveStringLiteralDfa2_2(active0, 0x800000000000L);
      case 85:
         return jjMoveStringLiteralDfa2_2(active0, 0x8000000000000L);
      case 97:
         return jjMoveStringLiteralDfa2_2(active0, 0x1000000000000L);
      case 102:
         if ((active0 & 0x4000000000000L) != 0L)
         {
            jjmatchedKind = 50;
            jjmatchedPos = 1;
         }
         break;
      case 111:
         return jjMoveStringLiteralDfa2_2(active0, 0x2000000000000L);
      case 114:
         return jjMoveStringLiteralDfa2_2(active0, 0x800000000000L);
      case 117:
         return jjMoveStringLiteralDfa2_2(active0, 0x8000000000000L);
      case 124:
         if ((active0 & 0x40000000000L) != 0L)
         {
            jjmatchedKind = 42;
            jjmatchedPos = 1;
         }
         break;
      default :
         break;
   }
   return jjMoveNfa_2(0, 1);
}
private final int jjMoveStringLiteralDfa2_2(long old0, long active0)
{
   if (((active0 &= old0)) == 0L)
      return jjMoveNfa_2(0, 1);
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
   return jjMoveNfa_2(0, 1);
   }
   switch(curChar)
   {
      case 76:
         return jjMoveStringLiteralDfa3_2(active0, 0x9000000000000L);
      case 79:
         return jjMoveStringLiteralDfa3_2(active0, 0x2000000000000L);
      case 85:
         return jjMoveStringLiteralDfa3_2(active0, 0x800000000000L);
      case 108:
         return jjMoveStringLiteralDfa3_2(active0, 0x9000000000000L);
      case 111:
         return jjMoveStringLiteralDfa3_2(active0, 0x2000000000000L);
      case 117:
         return jjMoveStringLiteralDfa3_2(active0, 0x800000000000L);
      default :
         break;
   }
   return jjMoveNfa_2(0, 2);
}
private final int jjMoveStringLiteralDfa3_2(long old0, long active0)
{
   if (((active0 &= old0)) == 0L)
      return jjMoveNfa_2(0, 2);
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
   return jjMoveNfa_2(0, 2);
   }
   switch(curChar)
   {
      case 69:
         if ((active0 & 0x800000000000L) != 0L)
         {
            jjmatchedKind = 47;
            jjmatchedPos = 3;
         }
         break;
      case 76:
         if ((active0 & 0x2000000000000L) != 0L)
         {
            jjmatchedKind = 49;
            jjmatchedPos = 3;
         }
         else if ((active0 & 0x8000000000000L) != 0L)
         {
            jjmatchedKind = 51;
            jjmatchedPos = 3;
         }
         break;
      case 83:
         return jjMoveStringLiteralDfa4_2(active0, 0x1000000000000L);
      case 101:
         if ((active0 & 0x800000000000L) != 0L)
         {
            jjmatchedKind = 47;
            jjmatchedPos = 3;
         }
         break;
      case 108:
         if ((active0 & 0x2000000000000L) != 0L)
         {
            jjmatchedKind = 49;
            jjmatchedPos = 3;
         }
         else if ((active0 & 0x8000000000000L) != 0L)
         {
            jjmatchedKind = 51;
            jjmatchedPos = 3;
         }
         break;
      case 115:
         return jjMoveStringLiteralDfa4_2(active0, 0x1000000000000L);
      default :
         break;
   }
   return jjMoveNfa_2(0, 3);
}
private final int jjMoveStringLiteralDfa4_2(long old0, long active0)
{
   if (((active0 &= old0)) == 0L)
      return jjMoveNfa_2(0, 3);
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
   return jjMoveNfa_2(0, 3);
   }
   switch(curChar)
   {
      case 69:
         if ((active0 & 0x1000000000000L) != 0L)
         {
            jjmatchedKind = 48;
            jjmatchedPos = 4;
         }
         break;
      case 101:
         if ((active0 & 0x1000000000000L) != 0L)
         {
            jjmatchedKind = 48;
            jjmatchedPos = 4;
         }
         break;
      default :
         break;
   }
   return jjMoveNfa_2(0, 4);
}
private final void jjCheckNAdd(int state)
{
   if (jjrounds[state] != jjround)
   {
      jjstateSet[jjnewStateCnt++] = state;
      jjrounds[state] = jjround;
   }
}
private final void jjAddStates(int start, int end)
{
   do {
      jjstateSet[jjnewStateCnt++] = jjnextStates[start];
   } while (start++ != end);
}
private final void jjCheckNAddTwoStates(int state1, int state2)
{
   jjCheckNAdd(state1);
   jjCheckNAdd(state2);
}
private final void jjCheckNAddStates(int start, int end)
{
   do {
      jjCheckNAdd(jjnextStates[start]);
   } while (start++ != end);
}
private final void jjCheckNAddStates(int start)
{
   jjCheckNAdd(jjnextStates[start]);
   jjCheckNAdd(jjnextStates[start + 1]);
}
private final int jjMoveNfa_2(int startState, int curPos)
{
   int strKind = jjmatchedKind;
   int strPos = jjmatchedPos;
   int seenUpto;
   input_stream.backup(seenUpto = curPos + 1);
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) { throw new Error("Internal Error"); }
   curPos = 0;
   int[] nextStates;
   int startsAt = 0;
   jjnewStateCnt = 3;
   int i = 1;
   jjstateSet[0] = startState;
   int j, kind = 0x7fffffff;
   for (;;)
   {
      if (++jjround == 0x7fffffff)
         ReInitRounds();
      if (curChar < 64)
      {
         long l = 1L << curChar;
         MatchLoop: do
         {
            switch(jjstateSet[--i])
            {
               case 0:
               case 2:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 53)
                     kind = 53;
                  jjCheckNAdd(2);
                  break;
               case 1:
                  if ((0xfff000000000000L & l) == 0L)
                     break;
                  if (kind > 52)
                     kind = 52;
                  jjstateSet[jjnewStateCnt++] = 1;
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      else if (curChar < 128)
      {
         long l = 1L << (curChar & 077);
         MatchLoop: do
         {
            switch(jjstateSet[--i])
            {
               case 0:
               case 1:
                  if ((0x7fffffe87fffffeL & l) == 0L)
                     break;
                  if (kind > 52)
                     kind = 52;
                  jjCheckNAdd(1);
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      else
      {
         int hiByte = (int)(curChar >> 8);
         int i1 = hiByte >> 6;
         long l1 = 1L << (hiByte & 077);
         int i2 = (curChar & 0xff) >> 6;
         long l2 = 1L << (curChar & 077);
         MatchLoop: do
         {
            switch(jjstateSet[--i])
            {
               default : break;
            }
         } while(i != startsAt);
      }
      if (kind != 0x7fffffff)
      {
         jjmatchedKind = kind;
         jjmatchedPos = curPos;
         kind = 0x7fffffff;
      }
      ++curPos;
      if ((i = jjnewStateCnt) == (startsAt = 3 - (jjnewStateCnt = startsAt)))
         break;
      try { curChar = input_stream.readChar(); }
      catch(java.io.IOException e) { break; }
   }
   if (jjmatchedPos > strPos)
      return curPos;

   int toRet = Math.max(curPos, seenUpto);

   if (curPos < toRet)
      for (i = toRet - Math.min(curPos, seenUpto); i-- > 0; )
         try { curChar = input_stream.readChar(); }
         catch(java.io.IOException e) { throw new Error("Internal Error : Please send a bug report."); }

   if (jjmatchedPos < strPos)
   {
      jjmatchedKind = strKind;
      jjmatchedPos = strPos;
   }
   else if (jjmatchedPos == strPos && jjmatchedKind > strKind)
      jjmatchedKind = strKind;

   return toRet;
}
private final int jjMoveStringLiteralDfa0_3()
{
   switch(curChar)
   {
      case 62:
         return jjStopAtPos(0, 14);
      case 92:
         return jjMoveStringLiteralDfa1_3(0x18000L);
      case 123:
         return jjStopAtPos(0, 13);
      default :
         return 1;
   }
}
private final int jjMoveStringLiteralDfa1_3(long active0)
{
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      return 1;
   }
   switch(curChar)
   {
      case 62:
         if ((active0 & 0x10000L) != 0L)
            return jjStopAtPos(1, 16);
         break;
      case 123:
         if ((active0 & 0x8000L) != 0L)
            return jjStopAtPos(1, 15);
         break;
      default :
         return 2;
   }
   return 2;
}
private final int jjMoveStringLiteralDfa0_0()
{
   switch(curChar)
   {
      case 39:
         return jjStopAtPos(0, 59);
      case 92:
         return jjMoveStringLiteralDfa1_0(0x1000000000000000L);
      default :
         return 1;
   }
}
private final int jjMoveStringLiteralDfa1_0(long active0)
{
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      return 1;
   }
   switch(curChar)
   {
      case 39:
         if ((active0 & 0x1000000000000000L) != 0L)
            return jjStopAtPos(1, 60);
         break;
      default :
         return 2;
   }
   return 2;
}
private final int jjMoveStringLiteralDfa0_1()
{
   switch(curChar)
   {
      case 34:
         return jjStopAtPos(0, 56);
      case 92:
         return jjMoveStringLiteralDfa1_1(0x200000000000000L);
      default :
         return 1;
   }
}
private final int jjMoveStringLiteralDfa1_1(long active0)
{
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      return 1;
   }
   switch(curChar)
   {
      case 34:
         if ((active0 & 0x200000000000000L) != 0L)
            return jjStopAtPos(1, 57);
         break;
      default :
         return 2;
   }
   return 2;
}
static final int[] jjnextStates = {
};
public static final String[] jjstrLiteralImages = {
null, null, null, null, null, null, null, null, "\74", null, null, null, null, 
null, null, null, null, null, null, null, null, null, "\44", "\75\75", "\41\75", 
"\76", "\76\75", "\74", "\74\75", "\133", "\135", "\50", "\51", "\56", "\52", 
"\53\53", "\53", "\55\55", "\55", "\57", "\45", "\46\46", "\174\174", "\54", "\100", 
"\41", "\175", null, null, null, null, null, null, null, null, null, null, null, null, 
null, null, null, };
public static final String[] lexStateNames = {
   "SQ_STR", 
   "DQ_STR", 
   "SCRIPT", 
   "BODY_STR", 
   "BASE", 
   "DEFAULT", 
};
public static final int[] jjnewLexState = {
   -1, -1, -1, -1, -1, -1, -1, -1, 4, 2, 5, -1, 3, 2, 5, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 
   -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 4, -1, -1, -1, 
   -1, -1, -1, -1, 1, 0, 2, -1, -1, 2, -1, -1, 
};
static final long[] jjtoToken = {
   0x93fffffffc06701L, 
};
static final long[] jjtoSkip = {
   0x3c0000L, 
};
static final long[] jjtoMore = {
   0x36c0000000039800L, 
};
protected SimpleCharStream input_stream;
private final int[] jjrounds = new int[3];
private final int[] jjstateSet = new int[6];
StringBuffer image;
int jjimageLen;
int lengthOfMatch;
protected char curChar;
public AlinousAttrScriptParserTokenManager(SimpleCharStream stream){
   if (SimpleCharStream.staticFlag)
      throw new Error("ERROR: Cannot use a static CharStream class with a non-static lexical analyzer.");
   input_stream = stream;
}
public AlinousAttrScriptParserTokenManager(SimpleCharStream stream, int lexState){
   this(stream);
   SwitchTo(lexState);
}
public void ReInit(SimpleCharStream stream)
{
   jjmatchedPos = jjnewStateCnt = 0;
   curLexState = defaultLexState;
   input_stream = stream;
   ReInitRounds();
}
private final void ReInitRounds()
{
   int i;
   jjround = 0x80000001;
   for (i = 3; i-- > 0;)
      jjrounds[i] = 0x80000000;
}
public void ReInit(SimpleCharStream stream, int lexState)
{
   ReInit(stream);
   SwitchTo(lexState);
}
public void SwitchTo(int lexState)
{
   if (lexState >= 6 || lexState < 0)
      throw new TokenMgrError("Error: Ignoring invalid lexical state : " + lexState + ". State unchanged.", TokenMgrError.INVALID_LEXICAL_STATE);
   else
      curLexState = lexState;
}

protected Token jjFillToken()
{
   Token t = Token.newToken(jjmatchedKind);
   t.kind = jjmatchedKind;
   String im = jjstrLiteralImages[jjmatchedKind];
   t.image = (im == null) ? input_stream.GetImage() : im;
   t.beginLine = input_stream.getBeginLine();
   t.beginColumn = input_stream.getBeginColumn();
   t.endLine = input_stream.getEndLine();
   t.endColumn = input_stream.getEndColumn();
   return t;
}

int curLexState = 5;
int defaultLexState = 5;
int jjnewStateCnt;
int jjround;
int jjmatchedPos;
int jjmatchedKind;

public Token getNextToken() 
{
  int kind;
  Token specialToken = null;
  Token matchedToken;
  int curPos = 0;

  EOFLoop :
  for (;;)
  {   
   try   
   {     
      curChar = input_stream.BeginToken();
   }     
   catch(java.io.IOException e)
   {        
      jjmatchedKind = 0;
      matchedToken = jjFillToken();
      return matchedToken;
   }
   image = null;
   jjimageLen = 0;

   for (;;)
   {
     switch(curLexState)
     {
       case 0:
         jjmatchedKind = 0x7fffffff;
         jjmatchedPos = 0;
         curPos = jjMoveStringLiteralDfa0_0();
         if (jjmatchedPos == 0 && jjmatchedKind > 61)
         {
            jjmatchedKind = 61;
         }
         break;
       case 1:
         jjmatchedKind = 0x7fffffff;
         jjmatchedPos = 0;
         curPos = jjMoveStringLiteralDfa0_1();
         if (jjmatchedPos == 0 && jjmatchedKind > 58)
         {
            jjmatchedKind = 58;
         }
         break;
       case 2:
         jjmatchedKind = 0x7fffffff;
         jjmatchedPos = 0;
         curPos = jjMoveStringLiteralDfa0_2();
         break;
       case 3:
         jjmatchedKind = 0x7fffffff;
         jjmatchedPos = 0;
         curPos = jjMoveStringLiteralDfa0_3();
         if (jjmatchedPos == 0 && jjmatchedKind > 17)
         {
            jjmatchedKind = 17;
         }
         break;
       case 4:
         jjmatchedKind = 0x7fffffff;
         jjmatchedPos = 0;
         curPos = jjMoveStringLiteralDfa0_4();
         if (jjmatchedPos == 0 && jjmatchedKind > 12)
         {
            jjmatchedKind = 12;
         }
         break;
       case 5:
         jjmatchedKind = 0x7fffffff;
         jjmatchedPos = 0;
         curPos = jjMoveStringLiteralDfa0_5();
         break;
     }
     if (jjmatchedKind != 0x7fffffff)
     {
        if (jjmatchedPos + 1 < curPos)
           input_stream.backup(curPos - jjmatchedPos - 1);
        if ((jjtoToken[jjmatchedKind >> 6] & (1L << (jjmatchedKind & 077))) != 0L)
        {
           matchedToken = jjFillToken();
           TokenLexicalActions(matchedToken);
       if (jjnewLexState[jjmatchedKind] != -1)
         curLexState = jjnewLexState[jjmatchedKind];
           return matchedToken;
        }
        else if ((jjtoSkip[jjmatchedKind >> 6] & (1L << (jjmatchedKind & 077))) != 0L)
        {
         if (jjnewLexState[jjmatchedKind] != -1)
           curLexState = jjnewLexState[jjmatchedKind];
           continue EOFLoop;
        }
        MoreLexicalActions();
      if (jjnewLexState[jjmatchedKind] != -1)
        curLexState = jjnewLexState[jjmatchedKind];
        curPos = 0;
        jjmatchedKind = 0x7fffffff;
        try {
           curChar = input_stream.readChar();
           continue;
        }
        catch (java.io.IOException e1) { }
     }
     int error_line = input_stream.getEndLine();
     int error_column = input_stream.getEndColumn();
     String error_after = null;
     boolean EOFSeen = false;
     try { input_stream.readChar(); input_stream.backup(1); }
     catch (java.io.IOException e1) {
        EOFSeen = true;
        error_after = curPos <= 1 ? "" : input_stream.GetImage();
        if (curChar == '\n' || curChar == '\r') {
           error_line++;
           error_column = 0;
        }
        else
           error_column++;
     }
     if (!EOFSeen) {
        input_stream.backup(1);
        error_after = curPos <= 1 ? "" : input_stream.GetImage();
     }
     throw new TokenMgrError(EOFSeen, curLexState, error_line, error_column, error_after, curChar, TokenMgrError.LEXICAL_ERROR);
   }
  }
}

void MoreLexicalActions()
{
   jjimageLen += (lengthOfMatch = jjmatchedPos + 1);
   switch(jjmatchedKind)
   {
      case 11 :
         if (image == null)
            image = new StringBuffer();
         image.append(input_stream.GetSuffix(jjimageLen));
         jjimageLen = 0;
                image.delete(image.length() - 2, image.length());
                image.append("{");
         break;
      case 15 :
         if (image == null)
            image = new StringBuffer();
         image.append(input_stream.GetSuffix(jjimageLen));
         jjimageLen = 0;
                image.delete(image.length() - 2, image.length());
                image.append("{");
         break;
      default : 
         break;
   }
}
void TokenLexicalActions(Token matchedToken)
{
   switch(jjmatchedKind)
   {
      case 13 :
        if (image == null)
            image = new StringBuffer();
            image.append(input_stream.GetSuffix(jjimageLen + (lengthOfMatch = jjmatchedPos + 1)));
                if(image.length() >= 2 && image.charAt(image.length() - 1) == '{'){
                        matchedToken.image = image.substring(0, image.length() - 1);
                }
         break;
      case 14 :
        if (image == null)
            image = new StringBuffer();
            image.append(input_stream.GetSuffix(jjimageLen + (lengthOfMatch = jjmatchedPos + 1)));
                if(image.length() >= 2 && image.charAt(image.length() - 1) == '>'){
                        matchedToken.image = image.substring(0, image.length() - 1);
                }
         break;
      case 56 :
        if (image == null)
            image = new StringBuffer();
            image.append(input_stream.GetSuffix(jjimageLen + (lengthOfMatch = jjmatchedPos + 1)));
                matchedToken.image = image.substring(1, image.length() - 1);
         break;
      case 59 :
        if (image == null)
            image = new StringBuffer();
            image.append(input_stream.GetSuffix(jjimageLen + (lengthOfMatch = jjmatchedPos + 1)));
                matchedToken.image = image.substring(1, image.length() - 1);
         break;
      default : 
         break;
   }
}
}

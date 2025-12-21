package com.opensourcereader.core.analysis.service.impl;

import org.springframework.stereotype.Service;

import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.type.Type;

@Service
public class ExpressionTypeResolver {

  public String getTypeToString(Expression arg) {
    if (arg == null) {
      return "Unknown";
    }

    // 0) 괄호는 무조건 벗겨
    arg = unwrapEnclosed(arg);

    // 1) null  -> 받았을떄 추가 처리필요
    if (arg.isNullLiteralExpr()) {
      return "null";
    }

    // 2) 리터럴들 (확정)
    if (arg.isStringLiteralExpr() || arg.isTextBlockLiteralExpr()) {
      return "String";
    }
    if (arg.isBooleanLiteralExpr()) {
      return "boolean";
    }
    if (arg.isCharLiteralExpr()) {
      return "char";
    }
    if (arg.isIntegerLiteralExpr()) {
      return "int";
    }
    if (arg.isLongLiteralExpr()) {
      return "long";
    }
    if (arg.isDoubleLiteralExpr()) {
      return "double";
    }

    // 3) new 생성 (확정)
    if (arg.isObjectCreationExpr()) {
      // new ArrayList<String>() -> "ArrayList<String>"
      return arg.asObjectCreationExpr().getType().toString();
    }

    // 4) 캐스팅 (확정)
    if (arg.isCastExpr()) {
      // (Rate) obj -> "Rate", (long)10 -> "long"
      return arg.asCastExpr().getType().toString();
    }

    // 5) instanceof (확정: boolean)
    if (arg.isInstanceOfExpr()) {
      return "boolean";
    }

    // 6) Class literal (Rate.class)
    if (arg.isClassExpr()) {
      // 엄밀히는 Class<Rate>지만, MVP에선 "Class" 또는 "Class<Rate>"
      String inner = arg.asClassExpr().getType().toString();
      return "Class<" + inner + ">";
    }

    // 변수로 찾아들어가야함
    // 7) 배열 생성: new int[10], new Rate[]{...}
    if (arg.isArrayCreationExpr()) {
      Type component = arg.asArrayCreationExpr().getElementType();
      // 차원 수까지 표현하고 싶으면 getLevels()로 처리 가능
      return component.toString() + "[]";
    }

    // 8) 배열 접근: arr[i] -> element type을 모르니 태깅
    if (arg.isArrayAccessExpr()) {
      // 최소한 “배열 접근”임은 기록해두는 게 유용
      return "Unknown(ArrayAccess)";
    }

    // 연산자는 붙을 수 있는 연산자의 종류에 따른 타입 지정
    // 9) Unary(피연산자 하나에만 적용되는 연산): !flag, -x, ++i  -> 받았을떄 추가 처리필요
    if (arg.isUnaryExpr()) {
      return inferUnary(arg.asUnaryExpr());
    }

    // 10) Binary: a+b, x>y, a&&b, "a"+b
    if (arg.isBinaryExpr()) {
      return inferBinary(arg.asBinaryExpr());
    }

    // 11) Conditional: cond ? a : b  -> 받았을떄 추가 처리필요(변수 처리 또는 반환태상에 따라 표현해야함)
    if (arg.isConditionalExpr()) {
      ConditionalExpr ce = arg.asConditionalExpr();
      String thenT = getTypeToString(ce.getThenExpr());
      String elseT = getTypeToString(ce.getElseExpr());
      if (thenT.equals(elseT)) {
        return thenT;
      }
      // null 섞이면 "nullable"로 태깅할 수도 있음
      if ("null".equals(thenT)) {
        return elseT + "?";
      }
      if ("null".equals(elseT)) {
        return thenT + "?";
      }
      return "Unknown(Conditional)";
    }

    // 13) Method call / reference / lambda: 파서만으론 타입 확정 불가 -> 태깅  -> 받았을떄 추가 처리필요
    if (arg.isMethodCallExpr()) {
      return "Unknown(MethodCall)"; // 메서드 콜한 부분
    }
    if (arg.isMethodReferenceExpr()) {
      return "Unknown(MethodRef)"; //  String::length 같은 메서드 레퍼런스
    }
    if (arg.isLambdaExpr()) {
      return "Unknown(Lambda)"; // 람다식
    }
    if (arg.isSwitchExpr()) {
      return "Unknown(SwitchExpr)"; // switch문
    }

    // 14) Field access / Name / This / Super: 컨텍스트 없으면 확정 불가
    if (arg.isFieldAccessExpr()) {
      return "Unknown(FieldAccess)"; // <scope>.<name>에서 name이 필드 이름일떄
    }
    if (arg.isNameExpr()) {
      return "Unknown(Name)"; // 상수 같은 이름 표현들
    }
    if (arg.isThisExpr()) {
      return "Unknown(this)"; // 오직 단독 this / 단독 super -> 상속 구조를 알고 있어야함
    }
    if (arg.isSuperExpr()) {
      return "Unknown(super)";
    }

    // 15) 패턴 매칭
    if (arg.isPatternExpr()) {
      if (arg.isTypePatternExpr()) {
        return "Unknown(TypePattern)"; // instanceOf
      }
      if (arg.isRecordPatternExpr()) {
        return "Unknown(RecordPattern)";
      }
      return "Unknown(Pattern)";
    }

    // 매개변수내에서 할당은 불가
    // 12) Assign(할당): (a = b) 같은 게 인자로 들어갈 때. 결과는 RHS 타입에 가까움
    if (arg.isAssignExpr()) {
      AssignExpr ae = arg.asAssignExpr();
      return getTypeToString(ae.getValue());
    }

    return "Unknown";
  }

  private Expression unwrapEnclosed(Expression e) {
    Expression cur = e;
    while (cur.isEnclosedExpr()) {
      cur = cur.asEnclosedExpr().getInner();
    }
    return cur;
  }

  // 타입을 가능
  private String inferUnary(UnaryExpr ue) {
    // operand 기반. 파서-only best effort
    UnaryExpr.Operator op = ue.getOperator();

    if (op == UnaryExpr.Operator.LOGICAL_COMPLEMENT) {
      return "boolean";
    }

    // ++i, i++, --i, i-- 는 피연산자 타입 유지라고 가정
    if (op == UnaryExpr.Operator.PREFIX_INCREMENT
        || op == UnaryExpr.Operator.PREFIX_DECREMENT
        || op == UnaryExpr.Operator.POSTFIX_INCREMENT
        || op == UnaryExpr.Operator.POSTFIX_DECREMENT) {
      return getTypeToString(ue.getExpression());
    }

    // +x, -x, ~x 같은 숫자/비트 연산은 operand 타입을 유지(정확히는 numeric promotion 존재)
    if (op == UnaryExpr.Operator.PLUS
        || op == UnaryExpr.Operator.MINUS
        || op == UnaryExpr.Operator.BITWISE_COMPLEMENT) {
      return getTypeToString(ue.getExpression());
    }

    return "Unknown(Unary)";
  }

  private String inferBinary(BinaryExpr be) {
    BinaryExpr.Operator op = be.getOperator();

    // 비교/논리 연산 결과는 boolean 확정
    switch (op) {
      case EQUALS, NOT_EQUALS, LESS, LESS_EQUALS, GREATER, GREATER_EQUALS, AND, OR -> {
        return "boolean";
      }
      default -> {
        /* continue */
      }
    }

    // 산술/비트/시프트/PLUS(문자열 결합 포함)
    String leftT = getTypeToString(be.getLeft());
    String rightT = getTypeToString(be.getRight());

    // String + anything -> String
    if (op == BinaryExpr.Operator.PLUS) {
      if (isStringType(leftT) || isStringType(rightT)) {
        return "String";
      }
    }

    // 숫자 결합 best effort: double > long > int
    if (isNumeric(leftT) && isNumeric(rightT)) {
      if ("double".equals(leftT) || "double".equals(rightT)) {
        return "double";
      }
      if ("long".equals(leftT) || "long".equals(rightT)) {
        return "long";
      }
      return "int";
    }

    // 비트/시프트도 숫자 취급
    if (isNumeric(leftT) && op.name().contains("SHIFT")) {
      return leftT;
    }
    if (op == BinaryExpr.Operator.BINARY_AND
        || op == BinaryExpr.Operator.BINARY_OR
        || op == BinaryExpr.Operator.XOR) {
      // boolean & boolean 도 가능하지만 파서-only에선 단순화
      if ("boolean".equals(leftT) && "boolean".equals(rightT)) {
        return "boolean";
      }
      if (isNumeric(leftT) && isNumeric(rightT)) {
        return promoteIntLong(leftT, rightT);
      }
      return "Unknown(Binary)";
    }

    return "Unknown(Binary)";
  }

  private boolean isStringType(String t) {
    return "String".equals(t) || "java.lang.String".equals(t);
  }

  private boolean isNumeric(String t) {
    return "int".equals(t) || "long".equals(t) || "double".equals(t);
  }

  private String promoteIntLong(String a, String b) {
    if ("long".equals(a) || "long".equals(b)) {
      return "long";
    }
    return "int";
  }
}

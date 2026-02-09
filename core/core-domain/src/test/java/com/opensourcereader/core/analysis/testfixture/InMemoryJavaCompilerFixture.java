package com.opensourcereader.core.analysis.testfixture;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

public final class InMemoryJavaCompilerFixture {

  public static Map<String, byte[]> compile(String mainClassFqcn, String fullSource) {
    // 1.컴파일러 여부 파악
    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    if (compiler == null) {
      throw new IllegalStateException("JDK가 필요합니다. (ToolProvider.getSystemJavaCompiler() == null)");
    }

    // 2. 바이트코드(.class) 파일의 임시 저장 공간 (in-memory) 설정
    DiagnosticCollector<JavaFileObject> diagnostics =
        new DiagnosticCollector<>(); // 컴파일 에러 / 경고 / 라인 정보 수집기
    StandardJavaFileManager std = compiler.getStandardFileManager(diagnostics, null, null);
    MemoryFileManager fileManager = new MemoryFileManager(std);

    // 3. 소스 파일 생성
    String fileName = mainClassFqcn.replace('.', '/') + ".java";
    JavaFileObject src = new SourceJavaFileObject(fileName, fullSource);

    // 4. 컴파일러 옵션설정(.class에 디버그 정보 제거(선택): 분석 결과에 라인넘버 등이 섞이는 걸 줄임)
    List<String> options = List.of("-g:none");

    // 5. 컴파일러 실행
    Boolean ok =
        compiler.getTask(null, fileManager, diagnostics, options, null, List.of(src)).call();

    // 6. 컴파일 실패처리
    if (!Boolean.TRUE.equals(ok)) {
      StringBuilder sb = new StringBuilder("컴파일 실패:\n");
      for (Diagnostic<?> d : diagnostics.getDiagnostics()) sb.append(d).append('\n');
      throw new IllegalArgumentException(sb.toString());
    }

    // 7. key: "t.Main" 같은 FQCN와 byte 반환
    return fileManager.getAllClassBytes();
  }

  public static Map<String, byte[]> compile(Map<String, String> sources) {
    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    if (compiler == null) {
      throw new IllegalStateException("JDK가 필요합니다.");
    }

    DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
    StandardJavaFileManager std = compiler.getStandardFileManager(diagnostics, null, null);
    MemoryFileManager fm = new MemoryFileManager(std);

    List<JavaFileObject> compilationUnits = new ArrayList<>();

    for (Map.Entry<String, String> entry : sources.entrySet()) {
      compilationUnits.add(new SourceJavaFileObject(entry.getKey(), entry.getValue()));
    }

    List<String> options = List.of("-g:none");

    Boolean ok = compiler.getTask(null, fm, diagnostics, options, null, compilationUnits).call();

    if (!Boolean.TRUE.equals(ok)) {
      StringBuilder sb = new StringBuilder("컴파일 실패:\n");
      for (Diagnostic<?> d : diagnostics.getDiagnostics()) {
        sb.append(d).append('\n');
      }
      throw new IllegalArgumentException(sb.toString());
    }

    return fm.getAllClassBytes();
  }

  // 소스파일 커스텀 객체
  private static final class SourceJavaFileObject extends SimpleJavaFileObject {
    private final String code;

    SourceJavaFileObject(String path, String code) {
      super(URI.create("string:///" + path), Kind.SOURCE);
      this.code = code;
    }

    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors) {
      return code;
    }
  }

  //  .class 파일의 임시 저장 공간
  private static final class MemoryFileManager
      extends ForwardingJavaFileManager<StandardJavaFileManager> {
    private final Map<String, ByteArrayOutputStream> buffers = new HashMap<>();

    MemoryFileManager(StandardJavaFileManager fileManager) {
      super(fileManager);
    }

    @Override
    public JavaFileObject getJavaFileForOutput(
        Location location, String className, JavaFileObject.Kind kind, FileObject sibling) {

      return new SimpleJavaFileObject(
          URI.create("mem:///" + className.replace('.', '/') + kind.extension), kind) {

        @Override
        public OutputStream openOutputStream() {
          ByteArrayOutputStream baos = new ByteArrayOutputStream();
          buffers.put(className, baos);
          return baos;
        }
      };
    }

    Map<String, byte[]> getAllClassBytes() {
      Map<String, byte[]> out = new HashMap<>();
      for (var e : buffers.entrySet()) out.put(e.getKey(), e.getValue().toByteArray());
      return out;
    }
  }

  private InMemoryJavaCompilerFixture() {}
}

package com.dongha.monitoring.jsonl;

import java.util.List;

/** parseLines() 결과: 파싱된 엔트리 목록과 아직 assistant에 연결되지 않은 마지막 user 프롬프트. */
public record ParseLinesResult(List<JsonlEntry> entries, String pendingUserPrompt) {}

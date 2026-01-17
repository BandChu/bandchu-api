#!/bin/bash

# 테스트 리포트 요약 및 열기 스크립트
# 사용법: ./scripts/show-test-report.sh [--open]
#   --open: HTML 리포트를 자동으로 브라우저에서 열기

REPORT_DIR="build/reports/tests/test"
HTML_REPORT="$REPORT_DIR/index.html"
AUTO_OPEN=false

# --open 옵션 확인
if [ "$1" = "--open" ] || [ "$1" = "-o" ]; then
    AUTO_OPEN=true
fi

# HTML 리포트가 있는지 확인
if [ ! -f "$HTML_REPORT" ]; then
    echo "테스트 리포트가 없습니다."
    echo "먼저 './gradlew test'를 실행하세요."
    exit 1
fi

# 테스트 결과 파일 확인 (XML 리포트에서 정보 추출)
TEST_RESULTS_DIR="build/test-results/test"
TOTAL_TESTS=0
PASSED_TESTS=0
FAILED_TESTS=0
SKIPPED_TESTS=0

if [ -d "$TEST_RESULTS_DIR" ]; then
    # XML 파일에서 테스트 결과 추출
    for xml_file in "$TEST_RESULTS_DIR"/*.xml; do
        if [ -f "$xml_file" ]; then
            tests=$(grep -o 'tests="[0-9]*"' "$xml_file" | grep -o '[0-9]*' | head -1)
            failures=$(grep -o 'failures="[0-9]*"' "$xml_file" | grep -o '[0-9]*' | head -1)
            skipped=$(grep -o 'skipped="[0-9]*"' "$xml_file" | grep -o '[0-9]*' | head -1)
            
            if [ -n "$tests" ]; then
                TOTAL_TESTS=$((TOTAL_TESTS + tests))
                if [ -n "$failures" ]; then
                    FAILED_TESTS=$((FAILED_TESTS + failures))
                fi
                if [ -n "$skipped" ]; then
                    SKIPPED_TESTS=$((SKIPPED_TESTS + skipped))
                fi
            fi
        fi
    done
    
    PASSED_TESTS=$((TOTAL_TESTS - FAILED_TESTS - SKIPPED_TESTS))
fi

# 결과 출력
echo "=========================================="
echo "테스트 리포트 요약"
echo "=========================================="
echo ""
echo "전체: $TOTAL_TESTS  |  성공: $PASSED_TESTS  |  실패: $FAILED_TESTS  |  스킵: $SKIPPED_TESTS"
echo ""
echo "HTML 리포트: $HTML_REPORT"
echo ""

# 성공/실패 상태에 따른 메시지
if [ $FAILED_TESTS -eq 0 ]; then
    echo "모든 테스트가 성공했습니다!"
else
    echo "실패한 테스트가 $FAILED_TESTS개 있습니다."
fi

# 자동 열기 옵션이 있거나 실패한 테스트가 있는 경우
if [ "$AUTO_OPEN" = true ] || [ $FAILED_TESTS -gt 0 ]; then
    echo ""
    echo "브라우저에서 리포트 열기..."
    if command -v explorer.exe &> /dev/null; then
        explorer.exe "$(wslpath -w "$HTML_REPORT")" 2>/dev/null || explorer.exe "$HTML_REPORT"
    elif command -v xdg-open &> /dev/null; then
        xdg-open "$HTML_REPORT" 2>/dev/null
    elif command -v open &> /dev/null; then
        open "$HTML_REPORT" 2>/dev/null
    else
        echo "브라우저를 자동으로 열 수 없습니다. 직접 파일을 열어주세요:"
        echo "  $HTML_REPORT"
    fi
else
    echo ""
    echo "브라우저로 열기: ./scripts/show-test-report.sh --open"
fi
echo ""

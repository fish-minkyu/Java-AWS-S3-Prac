1. s3 버킷 삭제가 안됨
2. image entity를 여러번 저장하지 말고 한꺼번에 저장
3. Board의 imageList가 초기화되지 않아 null pointer exception 발생
4. 이미지가 없을 때에도 저장할 수 있게 하는 분기처리
5. RequestPart, RequestBody
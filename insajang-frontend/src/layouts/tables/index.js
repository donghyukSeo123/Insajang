import React, { useState } from "react";

// @mui material components
import Grid from "@mui/material/Grid";
import Card from "@mui/material/Card";
import ToggleButton from "@mui/material/ToggleButton";
import ToggleButtonGroup from "@mui/material/ToggleButtonGroup";
import Divider from "@mui/material/Divider";
import IconButton from "@mui/material/IconButton";
import ArrowBackIosIcon from "@mui/icons-material/ArrowBackIos";
import ArrowForwardIosIcon from "@mui/icons-material/ArrowForwardIos";

// Material Dashboard 2 React components
import MDBox from "components/MDBox";
import MDTypography from "components/MDTypography";
import MDInput from "components/MDInput";
import MDButton from "components/MDButton";

// Material Dashboard 2 React example components
import DashboardLayout from "examples/LayoutContainers/DashboardLayout";
import DashboardNavbar from "examples/Navbars/DashboardNavbar";
import Footer from "examples/Footer";

function Tables() {
  const [postContent, setPostContent] = useState("");
  const [scheduleTime, setScheduleTime] = useState("");
  const [uploadMode, setUploadMode] = useState("manual");
  const [mediaFiles, setMediaFiles] = useState([]); // 여러 미디어 파일 저장
  const [currentSlide, setCurrentSlide] = useState(0); // 현재 슬라이드 인덱스
  const [imagePrompt, setImagePrompt] = useState("");

  // 파일 선택 시 여러 미디어 파일 및 프리뷰 생성
  const handleMediaChange = (e) => {
    const files = Array.from(e.target.files);
    const newMedia = files.map((file) => ({
      file, // 실제 파일 객체도 저장
      url: URL.createObjectURL(file),
      type: file.type.startsWith("video") ? "video" : "image",
    }));
    setMediaFiles((prevMedia) => [...prevMedia, ...newMedia]); // 기존 미디어에 추가
    setCurrentSlide(mediaFiles.length + newMedia.length - 1); // 새로 추가된 마지막 슬라이드로 이동
  };

  const handleModeChange = (event, newMode) => {
    if (newMode !== null) {
      setUploadMode(newMode);
      setMediaFiles([]); // 모드 변경 시 미디어 파일 초기화
      setCurrentSlide(0);
    }
  };

  const handlePublish = () => {
    const data = {
      content: postContent,
      time: scheduleTime,
      mode: uploadMode,
      media:
        uploadMode === "manual"
          ? mediaFiles.map((m) => ({ name: m.file.name, type: m.type }))
          : imagePrompt,
    };
    alert(`게시글 예약 완료!\n데이터: ${JSON.stringify(data)}`);
    // 실제 서버 전송 로직은 여기에...
  };

  const goToPrevSlide = () => {
    setCurrentSlide((prev) => (prev === 0 ? mediaFiles.length - 1 : prev - 1));
  };

  const goToNextSlide = () => {
    setCurrentSlide((prev) => (prev === mediaFiles.length - 1 ? 0 : prev + 1));
  };

  return (
    <DashboardLayout>
      <DashboardNavbar />
      <MDBox pt={6} pb={3}>
        <Grid container spacing={3}>
          {/* 입력 폼 섹션 */}
          <Grid item xs={12} lg={7}>
            <Card>
              <MDBox
                mx={2}
                mt={-3}
                py={3}
                px={2}
                variant="gradient"
                bgColor="info"
                borderRadius="lg"
                coloredShadow="info"
              >
                <MDTypography variant="h6" color="white">
                  새 게시글 작성 및 예약
                </MDTypography>
              </MDBox>

              <MDBox p={4}>
                <MDBox mb={3} display="flex" flexDirection="column" alignItems="center">
                  <MDTypography variant="button" fontWeight="bold" mb={1}>
                    이미지/영상 등록 방식 선택
                  </MDTypography>
                  <ToggleButtonGroup
                    value={uploadMode}
                    exclusive
                    onChange={handleModeChange}
                    color="info"
                  >
                    <ToggleButton value="manual" sx={{ px: 3 }}>
                      직접 업로드
                    </ToggleButton>
                    <ToggleButton value="ai" sx={{ px: 3 }}>
                      AI 이미지 생성
                    </ToggleButton>
                  </ToggleButtonGroup>
                </MDBox>

                <MDBox mb={4} p={2} sx={{ border: "1px dashed #ddd", borderRadius: "8px" }}>
                  {uploadMode === "manual" ? (
                    <MDBox>
                      <MDTypography variant="h6" fontWeight="medium" mb={1}>
                        파일 업로드 (이미지/영상 다중 선택 가능)
                      </MDTypography>
                      <input
                        type="file"
                        accept="image/*,video/*"
                        multiple
                        onChange={handleMediaChange}
                        style={{ width: "100%" }}
                      />
                      {mediaFiles.length > 0 && (
                        <MDTypography variant="caption" color="text" mt={1} display="block">
                          선택된 파일: {mediaFiles.map((m) => m.file.name).join(", ")}
                        </MDTypography>
                      )}
                    </MDBox>
                  ) : (
                    <MDBox>
                      <MDTypography variant="h6" fontWeight="medium" mb={1}>
                        생성할 이미지 묘사 (AI)
                      </MDTypography>
                      <MDInput
                        fullWidth
                        placeholder="예: '꽃다발이 놓인 깔끔한 카페 테이블' (AI가 이미지 1개 생성)"
                        value={imagePrompt}
                        onChange={(e) => setImagePrompt(e.target.value)}
                      />
                    </MDBox>
                  )}
                </MDBox>

                <MDBox mb={4}>
                  <MDTypography variant="h6" fontWeight="medium" mb={1}>
                    게시글 내용
                  </MDTypography>
                  <MDInput
                    fullWidth
                    multiline
                    rows={4}
                    placeholder="인스타그램 문구를 입력하세요..."
                    value={postContent}
                    onChange={(e) => setPostContent(e.target.value)}
                  />
                </MDBox>

                <MDBox mb={4}>
                  <MDTypography variant="h6" fontWeight="medium" mb={1}>
                    예약 시간
                  </MDTypography>
                  <MDInput
                    type="datetime-local"
                    fullWidth
                    value={scheduleTime}
                    onChange={(e) => setScheduleTime(e.target.value)}
                  />
                </MDBox>

                <MDBox mt={4} display="flex" justifyContent="flex-end">
                  <MDButton variant="gradient" color="info" onClick={handlePublish}>
                    예약 등록하기
                  </MDButton>
                </MDBox>
              </MDBox>
            </Card>
          </Grid>

          {/* 프리뷰 섹션 */}
          <Grid item xs={12} lg={5}>
            <MDBox position="sticky" top="100px">
              <MDTypography variant="h6" fontWeight="bold" mb={2} textAlign="center">
                실시간 미리보기
              </MDTypography>
              <Card
                sx={{
                  maxWidth: "350px",
                  margin: "0 auto",
                  borderRadius: "20px",
                  overflow: "hidden",
                  border: "8px solid #333",
                  position: "relative",
                }}
              >
                <MDBox p={1.5} display="flex" alignItems="center">
                  <MDBox bgColor="grey-300" borderRadius="50%" width="30px" height="30px" mr={1} />
                  <MDTypography variant="button" fontWeight="bold">
                    insajang_official
                  </MDTypography>
                </MDBox>

                <MDBox
                  bgColor="#f0f2f5"
                  height="350px"
                  display="flex"
                  justifyContent="center"
                  alignItems="center"
                  sx={{ position: "relative" }}
                >
                  {mediaFiles.length > 0 && uploadMode === "manual" ? (
                    <>
                      {mediaFiles[currentSlide].type === "image" ? (
                        <img
                          src={mediaFiles[currentSlide].url}
                          alt="Preview"
                          style={{ width: "100%", height: "100%", objectFit: "cover" }}
                        />
                      ) : (
                        <video
                          src={mediaFiles[currentSlide].url}
                          controls
                          muted
                          autoPlay
                          loop
                          style={{ width: "100%", height: "100%", objectFit: "cover" }}
                        />
                      )}
                      {mediaFiles.length > 1 && (
                        <>
                          <IconButton
                            onClick={goToPrevSlide}
                            sx={{
                              position: "absolute",
                              left: 0,
                              top: "50%",
                              transform: "translateY(-50%)",
                              backgroundColor: "rgba(0,0,0,0.5)",
                              color: "white",
                              "&:hover": { backgroundColor: "rgba(0,0,0,0.7)" },
                            }}
                          >
                            <ArrowBackIosIcon fontSize="small" />
                          </IconButton>
                          <IconButton
                            onClick={goToNextSlide}
                            sx={{
                              position: "absolute",
                              right: 0,
                              top: "50%",
                              transform: "translateY(-50%)",
                              backgroundColor: "rgba(0,0,0,0.5)",
                              color: "white",
                              "&:hover": { backgroundColor: "rgba(0,0,0,0.7)" },
                            }}
                          >
                            <ArrowForwardIosIcon fontSize="small" />
                          </IconButton>
                          <MDTypography
                            variant="caption"
                            color="white"
                            sx={{
                              position: "absolute",
                              bottom: 8,
                              right: 8,
                              backgroundColor: "rgba(0,0,0,0.5)",
                              borderRadius: "4px",
                              px: 1,
                            }}
                          >
                            {currentSlide + 1} / {mediaFiles.length}
                          </MDTypography>
                        </>
                      )}
                    </>
                  ) : uploadMode === "ai" && imagePrompt ? (
                    <MDBox textAlign="center" px={3}>
                      <MDTypography variant="caption" color="info" fontWeight="bold">
                        AI 이미지 생성 예정:
                      </MDTypography>
                      <MDTypography variant="body2" fontStyle="italic">
                        &quot;{imagePrompt}&quot;
                      </MDTypography>
                    </MDBox>
                  ) : (
                    <MDTypography variant="caption" color="text">
                      이미지나 영상을 등록해주세요.
                    </MDTypography>
                  )}
                </MDBox>

                <MDBox p={2}>
                  <MDTypography variant="button" fontWeight="bold">
                    insajang_official
                  </MDTypography>
                  <MDTypography
                    variant="button"
                    color="text"
                    ml={1}
                    sx={{ whiteSpace: "pre-wrap" }}
                  >
                    {postContent || "내용이 여기에 표시됩니다."}
                  </MDTypography>
                  <Divider sx={{ my: 1 }} />
                  <MDTypography variant="caption" color="text">
                    {scheduleTime || "시간 미설정"}
                  </MDTypography>
                </MDBox>
              </Card>
            </MDBox>
          </Grid>
        </Grid>
      </MDBox>
      <Footer />
    </DashboardLayout>
  );
}

export default Tables;

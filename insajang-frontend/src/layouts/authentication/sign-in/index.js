import { useState } from "react";
import { Link } from "react-router-dom";

// @mui material components
import Card from "@mui/material/Card";
import Switch from "@mui/material/Switch";

// Material Dashboard components
import MDBox from "components/MDBox";
import MDTypography from "components/MDTypography";
import MDInput from "components/MDInput";
import MDButton from "components/MDButton";

// Layout
import BasicLayout from "layouts/authentication/components/BasicLayout";

// Image
import bgImage from "assets/images/bg-sign-in-basic.jpeg";
import signInLogo from "assets/images/logos/sign_in_logo.png";

function Basic() {
  const [rememberMe, setRememberMe] = useState(false);
  const handleSetRememberMe = () => setRememberMe(!rememberMe);
  
  return (
    <BasicLayout image={bgImage}>
      <Card
        sx={{
          maxWidth: 460,
          mx: "auto",
          mt: 8,          // 카드 위로 내림 (로고 숨 안 막힘)
          pt: 5,
          pb: 3,
        }}
      >
        <MDBox px={3}>
          {/* ===== 로고 영역 ===== */}
          <MDBox
            display="flex"
            flexDirection="column"
            alignItems="center"
            mb={4}
          >
            <MDBox
              component="img"
              src={signInLogo}
              alt="콘텐츠메이커 스튜디오 로고"
              sx={{
                width: 88,
                height: "auto",
                mb: 1.5,
              }}
            />

            <MDTypography variant="h5" fontWeight="bold">
              콘텐츠메이커 스튜디오
            </MDTypography>

            <MDTypography variant="caption" color="text">
              자동 콘텐츠 생성 · 예약 · 관리
            </MDTypography>
          </MDBox>

          {/* ===== 로그인 폼 ===== */}
          <MDBox component="form">
            <MDBox mb={2}>
              <MDInput type="email" label="이메일" fullWidth 
                variant="outlined"
                size="small"
              />
            </MDBox>

            <MDBox mb={2}>
              <MDInput type="password" label="비밀번호" fullWidth 
                variant="outlined"
                size="small"
              />
            </MDBox>

            <MDBox display="flex" alignItems="center" ml={-1}>
              <Switch checked={rememberMe} onChange={handleSetRememberMe} />
              <MDTypography
                variant="button"
                color="text"
                onClick={handleSetRememberMe}
                sx={{ cursor: "pointer", userSelect: "none", ml: -1 }}
              >
                로그인 상태 유지
              </MDTypography>
            </MDBox>

            <MDBox mt={4} mb={1}>
              <MDButton variant="gradient" color="info" fullWidth>
                로그인
              </MDButton>
            </MDBox>

            <MDBox mt={3} textAlign="center">
              <MDTypography variant="button" color="text">
                계정이 없으신가요?{" "}
                <MDTypography
                  component={Link}
                  to="/signup"
                  variant="button"
                  color="info"
                  fontWeight="medium"
                  textGradient
                >
                  회원가입
                </MDTypography>
              </MDTypography>
            </MDBox>
          </MDBox>
        </MDBox>
      </Card>
    </BasicLayout>
  );
}


export default Basic;

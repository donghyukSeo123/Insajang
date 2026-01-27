import { useState } from "react"; // 

// react-router-dom components
import { Link } from "react-router-dom";

// @mui material components
import Card from "@mui/material/Card";

// Material Dashboard 2 React components
import MDBox from "components/MDBox";
import MDTypography from "components/MDTypography";
import MDInput from "components/MDInput";
import MDButton from "components/MDButton";

// Authentication layout components
import CoverLayout from "layouts/authentication/components/CoverLayout";

// Images
import bgImage from "assets/images/bg-sign-up-cover.jpeg";

function Cover() {

  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");

  // 비밀번호가 서로 다를 때만 true (빨간 테두리용)
  const isError = password !== "" && confirmPassword !== "" && password !== confirmPassword;
  // 비밀번호가 같고 비어있지 않을 때 true (버튼 활성화용)
  const isMatch = password !== "" && password === confirmPassword;

  return (
    <CoverLayout image={bgImage}>
      <Card>
        <MDBox
          variant="gradient"
          bgColor="info"
          borderRadius="lg"
          coloredShadow="info"
          mx={2}
          mt={-3}
          p={3}
          mb={1}
          textAlign="center"
        >
          <MDTypography variant="h4" fontWeight="medium" color="white">
            회원가입
          </MDTypography>
          <MDTypography variant="button" color="white" my={1}>
            콘텐츠 자동 생성 서비스를 이용해보세요
          </MDTypography>
        </MDBox>

        <MDBox pt={4} pb={3} px={3}>
          <MDBox component="form" role="form">
            <MDBox mb={2}>
              <MDInput
                type="email"
                label="이메일"
                variant="outlined"
                size="small"
                fullWidth
              />
            </MDBox>

            <MDBox mb={2}>
              <MDInput
                type="password"
                label="비밀번호"
                variant="outlined"
                size="small"
                fullWidth
                value={password}
                onChange={(e) => setPassword(e.target.value)}
              />
            </MDBox>

            <MDBox mb={2}>
              <MDInput
                type="password"
                label="비밀번호 확인"
                variant="outlined"
                size="small"
                fullWidth
                error={isError} // 다르면 테두리가 빨갛게 변함!
                value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)}
              />
            </MDBox>
            
            <MDBox mb={2}>
              <MDInput
                type="email"
                label="닉네임"
                variant="outlined"
                size="small"
                fullWidth
              />
            </MDBox>

            <MDBox mt={4} mb={1}>
              <MDButton 
                variant="gradient" 
                color="info" 
                fullWidth
                disabled={!isMatch} // 일치할 때만 버튼 활성화
              >
                회원가입
              </MDButton>
            </MDBox>

            <MDBox mt={3} textAlign="center">
              <MDTypography variant="button" color="text">
                이미 계정이 있으신가요?{" "}
                <MDTypography
                  component={Link}
                  to="/login"
                  variant="button"
                  color="info"
                  fontWeight="medium"
                  textGradient
                >
                  로그인
                </MDTypography>
              </MDTypography>
            </MDBox>
          </MDBox>
        </MDBox>
      </Card>
    </CoverLayout>
  );
}

export default Cover;

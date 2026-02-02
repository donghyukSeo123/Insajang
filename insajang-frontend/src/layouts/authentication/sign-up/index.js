import { useState, useEffect} from "react"; // 

// react-router-dom components
import { Link } from "react-router-dom";

// @mui material components
import Card from "@mui/material/Card";

// Material Dashboard 2 React components
import MDBox from "components/MDBox";
import MDTypography from "components/MDTypography";
import MDInput from "components/MDInput";
import MDButton from "components/MDButton";
import API from "utils/api";

// Authentication layout components
import CoverLayout from "layouts/authentication/components/CoverLayout";

// Images
import bgImage from "assets/images/bg-sign-up-cover.jpeg";

function Cover() {

  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [isSent, setIsSent] = useState(false); // 메일 발송 여부
  const [timeLeft, setTimeLeft] = useState(180); // 3분 (180초)
  
  const passwordRegex = /^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d]{8,}$/;// 비밀번호 형식이 맞는지 (8자 이상, 영문/숫자)
  const isPasswordValid = passwordRegex.test(password);
  const isPasswordFormatValid = passwordRegex.test(password); // 형식 통과 여부
  const isPasswordEmpty = password === ""; //비밀번호비어있음
  const isConfirmEmpty = confirmPassword === "";  //비밀번호확인란 비어있음
  const isPasswordSame = password === confirmPassword; // 일치 여부
  const passwordError = !isPasswordEmpty && !isPasswordFormatValid; //패스워드 에러
  const confirmError = !isConfirmEmpty && (!isPasswordSame || !isPasswordFormatValid); //일치여부에러
  const confirmSuccess = !isConfirmEmpty && isPasswordSame && isPasswordFormatValid; //비밀번호사용가능여부

  const [email, setEmail] = useState(""); // 이메일 입력값 상태
  
  // 비밀번호 확인 칸 에러 여부 (비어있지 않은데 형식이 틀렸거나, 서로 다를 때)
  const isError = (password !== "" && !isPasswordValid) || 
                  (confirmPassword !== "" && password !== confirmPassword);
  // 모든 조건 충족 (형식도 맞고, 두 값도 일치함)
  const isMatch = isPasswordValid && password !== "" && password === confirmPassword;
  
  // 타이머 로직
  useEffect(() => {
    if (isSent && timeLeft > 0) {
      const timer = setInterval(() => {
        setTimeLeft((prev) => prev - 1);
      }, 1000);
      return () => clearInterval(timer); // 메모리 누수 방지
    } else if (timeLeft === 0) {
      setIsSent(false); // 시간 다 되면 다시 발송 가능하게 상태 변경
    }
  }, [isSent, timeLeft]);

  // 초를 분:초 형식으로 변환
  const formatTime = (seconds) => {
    const m = Math.floor(seconds / 60);
    const s = seconds % 60;
    return `${m}:${s < 10 ? `0${s}` : s}`;
  };
  
  const handleSendMail = async () => {
  if (!email) {
    alert("이메일을 입력해주세요!");
    return;
  }

  try {

    const response = await API.post("/api/user/email-verification", {
      email: email 
    });
    


    if (response.status === 200) {
      setIsSent(true);
      setTimeLeft(180);
      alert("인증번호가 발송되었습니다. 메일함을 확인해주세요!");
    }
  } catch (error) {
    console.error("메일 발송 실패:", error);
    alert("메일 발송에 실패했습니다. 이메일 주소를 확인하거나 잠시 후 다시 시도해주세요.");
  }
};

  


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
            <MDBox mb={2} display="flex" alignItems="center" gap={1}>
              {/* 이메일 입력창 - flex: 1로 설정해서 남는 공간을 다 차지하게 합니다 */}
              <MDBox sx={{ flex: 1 }}>
                <MDInput
                  type="email"
                  label="이메일"
                  variant="outlined"
                  size="small"
                  fullWidth
                  value={email} 
                  onChange={(e) => setEmail(e.target.value)} 
                />
              </MDBox>
              {/* 인증 버튼 & 타이머 묶음 */}
              <MDBox display="flex" alignItems="center" gap={1} sx={{ minWidth: "fit-content" }}>
                <MDButton 
                  variant="gradient" 
                  color={isSent ? "secondary" : "info"} // 발송 후에는 색상을 차분하게 변경
                  size="small"
                  disabled={isSent}
                  onClick={handleSendMail}
                  sx={{ height: "40px", whiteSpace: "nowrap" }} // 버튼 높이 맞춤 & 글자 줄바꿈 방지
                >
                  {isSent ? "발송완료" : "인증요청"}
                </MDButton>
                
                {/* 타이머: 버튼 바로 옆에 빨간색으로 표시 */}
                {isSent && (
                  <MDTypography 
                    variant="caption" 
                    color="error" 
                    fontWeight="bold" 
                    sx={{ minWidth: "35px", textAlign: "center" }}
                  >
                    {formatTime(timeLeft)}
                  </MDTypography>
                )}
              </MDBox>
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
              {!isPasswordFormatValid && !isPasswordEmpty && (
                <MDTypography variant="caption" color="error" fontWeight="bold" sx={{ ml: 1 }}>
                  영문, 숫자 조합 8자 이상으로 설정해주세요.
                </MDTypography>
              )}
            </MDBox>

            <MDBox mb={2}>
              <MDInput
                type="password"
                label="비밀번호 확인"
                variant="outlined"
                size="small"
                fullWidth
                error={isError}
                success={isMatch}
                value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)}
              />
              {/* 케이스별 센스 있는 메시지 */}
              {!isConfirmEmpty && !isPasswordSame && (
                <MDTypography variant="caption" color="error" fontWeight="bold" sx={{ ml: 1 }}>
                  먼저 입력한 비밀번호와 달라요! 다시 확인해주세요.
                </MDTypography>
              )}
              {confirmSuccess && (
                <MDTypography variant="caption" color="success" fontWeight="bold" sx={{ ml: 1 }}>
                  완벽합니다! 비밀번호가 일치해요.
                </MDTypography>
              )}
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

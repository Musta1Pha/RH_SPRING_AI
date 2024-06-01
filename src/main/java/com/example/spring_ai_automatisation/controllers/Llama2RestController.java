package com.example.spring_ai_automatisation.controllers;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
public class Llama2RestController {

    @Value("${lmstudio.api.url}")
    private String apiUrl;

    private final RestTemplate restTemplate;

    public Llama2RestController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @PostMapping(path = "/chat-with-file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Integer chatWithFile( @RequestParam("experience") int experience,
                                @RequestParam("education") int education,
                                @RequestParam("keywords") List<String> keywords,
                                @RequestParam("file") MultipartFile file) {
        String fileContent = extractTextFromPdf(file);
        if (fileContent == null) {
            return null;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String systemMessageContent = """
                You are an assistant working as a human resources profile.
                You will be asked to provide a percentage match between the CV and the job offer provided.
                """;

        String query = "I want a response containing only the final percentage of match of the CV compared to the defined information: the years of experience " + experience
                +", the years of education" + education + ", as well as the keywords to search" + keywords + ". you apply the sum of all the percentages values and you divide on 3, I don't want the details.";

        String userMessageContent = query + "\n\nContenu du fichier:\n" + fileContent;

        Map<String, String> systemMessage = Map.of("role", "system", "content", systemMessageContent);
        Map<String, String> userMessage = Map.of("role", "user", "content", userMessageContent);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("messages", List.of(systemMessage, userMessage));
        requestBody.put("temperature", 0);
        requestBody.put("max_tokens", 170);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> responseEntity = restTemplate.exchange(
                apiUrl,
                HttpMethod.POST,
                requestEntity,
                String.class
        );

        String responseBody = responseEntity.getBody();
        String content = null;
        if (responseBody != null) {
            try {
                JSONObject responseObject = new JSONObject(responseBody);
                JSONArray choicesArray = responseObject.getJSONArray("choices");
                if (choicesArray.length() > 0) {
                    JSONObject choiceObject = choicesArray.getJSONObject(0);
                    JSONObject messageObject = choiceObject.getJSONObject("message");
                    content = messageObject.getString("content");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        Pattern pattern = Pattern.compile("(\\d+)%");
        Matcher matcher = pattern.matcher(content);

        String Percentage = "";

        while (matcher.find()) {
            Percentage = matcher.group(1);
        }

        return Integer.parseInt(Percentage);
    }

    private String extractTextFromPdf(MultipartFile file) {
        PDDocument document = null;
        try {
            document = PDDocument.load(file.getInputStream());
            PDFTextStripper pdfStripper = new PDFTextStripper();
            return pdfStripper.getText(document);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            if (document != null) {
                try {
                    document.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

